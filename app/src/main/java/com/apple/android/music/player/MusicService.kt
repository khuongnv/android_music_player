package com.apple.android.music.player

import android.content.Intent
import android.util.Log
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import com.apple.android.music.domain.model.PlayerState
import com.apple.android.music.domain.repository.MusicRepository
import com.apple.android.music.domain.repository.PlayerStateRepository
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

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    // Coroutine scope for service operations (autosave ticker)
    // We use Dispatchers.Main because ExoPlayer must be accessed from the main thread
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var autosaveJob: Job? = null
    
    // Sleep Timer
    private var sleepTimerJob: Job? = null

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
        })

        // Restore latest state and start autosave ticker
        serviceScope.launch {
            restorePlayerState()
            startAutosaveTicker()
            observeSleepTimer()
        }
    }

    private fun observeSleepTimer() {
        serviceScope.launch {
            // Combine enabled and duration to react to changes
            kotlinx.coroutines.flow.combine(
                settingsRepository.sleepTimerEnabled,
                settingsRepository.sleepTimerDuration
            ) { enabled, duration ->
                Pair(enabled, duration)
            }.collect { (enabled, duration) ->
                manageSleepTimer(enabled, duration)
            }
        }
    }

    private fun manageSleepTimer(enabled: Boolean, durationMinutes: Int) {
        sleepTimerJob?.cancel()
        if (enabled && durationMinutes > 0) {
            Log.d("MusicApp", "MusicService: Sleep timer started for $durationMinutes minutes")
            sleepTimerJob = serviceScope.launch {
                delay(durationMinutes * 60 * 1000L)
                Log.d("MusicApp", "MusicService: Sleep timer expired. Pausing playback.")
                player?.pause()
                // Disable timer after it fires
                settingsRepository.setSleepTimerEnabled(false)
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
