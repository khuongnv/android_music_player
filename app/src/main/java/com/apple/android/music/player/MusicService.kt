package com.apple.android.music.player

import android.content.Intent
import android.util.Log
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import com.apple.android.music.domain.model.PlayerState
import com.apple.android.music.domain.repository.MusicRepository
import com.apple.android.music.domain.repository.PlayerStateRepository
import com.apple.android.music.domain.repository.TimelineRepository
import com.apple.android.music.util.toMediaItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import android.os.Bundle
import javax.inject.Inject
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi

@UnstableApi
@AndroidEntryPoint
class MusicService : MediaSessionService() {

    companion object {
        const val SAVE_STATE_COMMAND = "SAVE_STATE"
    }

    @Inject lateinit var playerStateRepository: PlayerStateRepository
    @Inject lateinit var musicRepository: MusicRepository
    @Inject lateinit var settingsRepository: com.apple.android.music.domain.repository.SettingsRepository
    @Inject lateinit var timelineRepository: TimelineRepository

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    // Coroutine scope for service operations (autosave ticker)
    // We use Dispatchers.Main because ExoPlayer must be accessed from the main thread
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var autosaveJob: Job? = null
    
    // Sleep Timer
    private var sleepTimerJob: Job? = null

    // 24h Playback Activity Tracking
    private var activityTrackingJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("MusicApp", "MusicService: onCreate")
        
        // Create ExoPlayer with audio attributes
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true // Handle audio focus
            )
            .setHandleAudioBecomingNoisy(true) // Pause when headphones disconnected
            .build()

        // Create MediaSession
        mediaSession = MediaSession.Builder(this, player!!)
            .setCallback(CustomMediaSessionCallback())
            .setBitmapLoader(CoilBitmapLoader(this))
            .build()
        Log.d("MusicApp", "MusicService: Player and MediaSession created")

        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e("MusicApp", "MusicService: PLAYER ERROR: ${error.message}", error)
            }

            override fun onPlaybackStateChanged(state: Int) {
                val stateName = when(state) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN"
                }
                Log.d("MusicApp", "MusicService: Playback state changed to $stateName")
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    serviceScope.launch { recordPlaybackActivity() }
                    startActivityTracking()
                } else {
                    activityTrackingJob?.cancel()
                    serviceScope.launch { recordPlaybackActivity() }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                serviceScope.launch { recordPlaybackActivity() }
            }
        })

        // Restore latest state and start autosave ticker
        serviceScope.launch {
            restorePlayerState()
            startAutosaveTicker()
            observeSleepTimer()
            timelineRepository.ensureSampleActivitiesIfEmpty()
        }
    }

    private fun startActivityTracking() {
        activityTrackingJob?.cancel()
        activityTrackingJob = serviceScope.launch {
            while (isActive) {
                delay(10000L) // Record activity progress every 10 seconds
                recordPlaybackActivity()
            }
        }
    }

    private suspend fun recordPlaybackActivity() {
        val currentPlayer = player ?: return
        val currentMediaItem = currentPlayer.currentMediaItem ?: return
        val songId = currentMediaItem.mediaId.toLongOrNull() ?: return
        val title = currentMediaItem.mediaMetadata.title?.toString() ?: "Unknown Title"
        val artist = currentMediaItem.mediaMetadata.artist?.toString() ?: "Unknown Artist"
        val album = currentMediaItem.mediaMetadata.albumTitle?.toString() ?: "Unknown Album"
        val artworkUri = currentMediaItem.mediaMetadata.artworkUri?.toString()
        
        // Ensure valid audio URI from repository, localConfiguration, or requestMetadata
        val realSong = musicRepository.getSongById(songId)
        val songUri = realSong?.uri?.toString()
            ?: currentMediaItem.localConfiguration?.uri?.toString()
            ?: currentMediaItem.requestMetadata.mediaUri?.toString()
            ?: ""

        val duration = if (currentPlayer.duration != C.TIME_UNSET && currentPlayer.duration > 0L) {
            currentPlayer.duration
        } else {
            realSong?.duration ?: 0L
        }
        val position = currentPlayer.currentPosition.coerceAtLeast(0L)

        timelineRepository.recordActivity(
            songId = songId,
            songTitle = realSong?.title ?: title,
            artistName = realSong?.artist ?: artist,
            albumName = realSong?.album ?: album,
            artworkUri = realSong?.albumArtUri?.toString() ?: artworkUri,
            songUri = songUri,
            songDuration = duration,
            currentPosition = position
        )
    }

    private fun observeSleepTimer() {
        serviceScope.launch {
            kotlinx.coroutines.flow.combine(
                settingsRepository.sleepTimerEnabled,
                settingsRepository.sleepTimerTargetTime
            ) { enabled, targetTime ->
                Pair(enabled, targetTime)
            }.collect { (enabled, targetTime) ->
                manageSleepTimer(enabled, targetTime)
            }
        }
    }

    private fun manageSleepTimer(enabled: Boolean, targetTimeMillis: Long) {
        sleepTimerJob?.cancel()
        if (enabled && targetTimeMillis > 0L) {
            val remainingDelay = targetTimeMillis - System.currentTimeMillis()
            Log.d("MusicApp", "MusicService: Sleep timer active. Target: $targetTimeMillis, Remaining: ${remainingDelay / 1000}s")
            if (remainingDelay <= 0L) {
                Log.d("MusicApp", "MusicService: Sleep timer already reached. Pausing playback.")
                player?.pause()
                serviceScope.launch {
                    settingsRepository.cancelSleepTimer()
                }
            } else {
                sleepTimerJob = serviceScope.launch {
                    delay(remainingDelay)
                    Log.d("MusicApp", "MusicService: Sleep timer expired. Pausing playback.")
                    player?.pause()
                    settingsRepository.cancelSleepTimer()
                }
            }
        } else {
            Log.d("MusicApp", "MusicService: Sleep timer disabled")
        }
    }

    private fun startAutosaveTicker() {
        autosaveJob?.cancel()
        autosaveJob = serviceScope.launch {
            while (isActive) {
                delay(60000) // 1 minute ticker
                saveCurrentState()
            }
        }
    }

    private suspend fun saveCurrentState() {
        val currentPlayer = player ?: return
        val currentMediaItem = currentPlayer.currentMediaItem
        val songId = currentMediaItem?.mediaId?.toLongOrNull() ?: -1L
        
        // Capture all song IDs in the current playlist
        val queueIds = ArrayList<Long>()
        val itemCount = currentPlayer.mediaItemCount
        for (i in 0 until itemCount) {
            val item = currentPlayer.getMediaItemAt(i)
            item.mediaId.toLongOrNull()?.let { queueIds.add(it) }
        }

        val state = PlayerState(
            songId = songId,
            queueIds = queueIds,
            position = currentPlayer.currentPosition,
            isPlaying = currentPlayer.isPlaying,
            repeatMode = currentPlayer.repeatMode,
            shuffleModeEnabled = currentPlayer.shuffleModeEnabled,
            playbackSpeed = currentPlayer.playbackParameters.speed
        )
        
        withContext(Dispatchers.IO) {
            playerStateRepository.savePlayerState(state)
        }
        Log.d("MusicApp", "MusicService: State saved. Song: $songId, Queue size: ${queueIds.size}")
    }

    private suspend fun restorePlayerState() {
        val state = withContext(Dispatchers.IO) {
            playerStateRepository.getLatestState()
        }
        
        if (state.queueIds.isNotEmpty()) {
            Log.d("MusicApp", "MusicService: Restoring queue with ${state.queueIds.size} songs")
            val allSongs = withContext(Dispatchers.IO) {
                musicRepository.getAllSongs()
            }
            
            // Reconstruct the queue in the correct order
            val queueSongs = state.queueIds.mapNotNull { id ->
                allSongs.find { it.id == id }
            }

            if (queueSongs.isNotEmpty()) {
                player?.apply {
                    setMediaItems(queueSongs.map { it.toMediaItem() })
                    
                    // Find the index of the previously playing song
                    val initialWindowIndex = state.queueIds.indexOf(state.songId).takeIf { it != -1 } ?: 0
                    
                    seekTo(initialWindowIndex, state.position)
                    repeatMode = state.repeatMode
                    shuffleModeEnabled = state.shuffleModeEnabled
                    setPlaybackParameters(PlaybackParameters(state.playbackSpeed))
                    
                    // Requirement: Always restore in PAUSED state
                    playWhenReady = false
                    prepare()
                }
                Log.d("MusicApp", "MusicService: Queue successfully restored")
            } else {
                Log.w("MusicApp", "MusicService: Failed to resolve songs from saved queue IDs")
            }

        } else if (state.songId != -1L) {
            // Fallback for old state without queue
            Log.d("MusicApp", "MusicService: Restoring single song state (legacy)")
            val song = withContext(Dispatchers.IO) {
                musicRepository.getSongById(state.songId)
            }
            
            song?.let { 
                player?.apply {
                    setMediaItem(it.toMediaItem())
                    seekTo(state.position)
                    repeatMode = state.repeatMode
                    shuffleModeEnabled = state.shuffleModeEnabled
                    setPlaybackParameters(PlaybackParameters(state.playbackSpeed))
                    
                    // Requirement: Always restore in PAUSED state
                    playWhenReady = false
                    prepare()
                }
                Log.d("MusicApp", "MusicService: Single song state restored")
            } ?: Log.w("MusicApp", "MusicService: Saved song not found in repository")
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        Log.d("MusicApp", "MusicService: onGetSession from ${controllerInfo.packageName}")
        return mediaSession
    }

    private inner class CustomMediaSessionCallback : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): com.google.common.util.concurrent.ListenableFuture<MutableList<MediaItem>> {
            val resolvedItems = mediaItems.map { item ->
                if (item.localConfiguration == null) {
                    val resolvedUri = item.requestMetadata.mediaUri
                        ?: item.mediaId.toLongOrNull()?.let { id ->
                            kotlinx.coroutines.runBlocking { musicRepository.getSongById(id)?.uri }
                        }
                    if (resolvedUri != null) {
                        item.buildUpon()
                            .setUri(resolvedUri)
                            .setRequestMetadata(
                                item.requestMetadata.buildUpon()
                                    .setMediaUri(resolvedUri)
                                    .build()
                            )
                            .build()
                    } else {
                        item
                    }
                } else {
                    item
                }
            }.toMutableList()
            return com.google.common.util.concurrent.Futures.immediateFuture(resolvedItems)
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: android.os.Bundle
        ): com.google.common.util.concurrent.ListenableFuture<androidx.media3.session.SessionResult> {
            if (customCommand.customAction == SAVE_STATE_COMMAND) {
                serviceScope.launch {
                    saveCurrentState()
                }
                return com.google.common.util.concurrent.Futures.immediateFuture(
                    androidx.media3.session.SessionResult(androidx.media3.session.SessionResult.RESULT_SUCCESS)
                )
            }
            return com.google.common.util.concurrent.Futures.immediateFuture(
                androidx.media3.session.SessionResult(androidx.media3.session.SessionResult.RESULT_ERROR_NOT_SUPPORTED)
            )
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        serviceScope.launch {
            saveCurrentState()
            val player = mediaSession?.player
            if (player != null && !player.playWhenReady) {
                // Stop the service if playback is paused
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        Log.d("MusicApp", "MusicService: onDestroy")
        serviceScope.launch {
            saveCurrentState()
            autosaveJob?.cancel()
            activityTrackingJob?.cancel()
            serviceScope.cancel()
        }
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        player = null
        super.onDestroy()
    }
}
