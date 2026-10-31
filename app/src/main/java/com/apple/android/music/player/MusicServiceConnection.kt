package com.apple.android.music.player

import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.apple.android.music.domain.model.Song
import com.apple.android.music.util.toMediaItem
import com.apple.android.music.util.toSong
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service connection helper for controlling music playback
 */
@Singleton
class MusicServiceConnection @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    
    private val connectionScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionUpdateJob: Job? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.let { item ->
                _currentSong.value = item.toSong(mediaController?.duration ?: 0L)
                _duration.value = mediaController?.duration ?: 0L
            }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _shuffleEnabled.value = shuffleModeEnabled
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = repeatMode
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlaybackMetadata()
            if (playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING) {
                startPositionUpdates()
            } else {
                // Keep updates running if we are still "ready" but paused is handled by state
                if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
                     startPositionUpdates()
                } else {
                     stopPositionUpdates()
                }
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            // Keep updates running even when paused so seeking works
            startPositionUpdates()
        }
    }

    private fun updatePlaybackMetadata() {
        mediaController?.let { controller ->
            val duration = if (controller.duration == androidx.media3.common.C.TIME_UNSET) 0L else controller.duration
            _duration.value = duration
            
            controller.currentMediaItem?.let { item ->
                _currentSong.value = item.toSong(duration)
            }
        }
    }

    private fun startPositionUpdates() {
        if (positionUpdateJob?.isActive == true) return
        
        positionUpdateJob = connectionScope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    _currentPosition.value = controller.currentPosition
                    // Also sync duration just in case it wasn't ready earlier
                    if (_duration.value <= 0L && controller.duration != androidx.media3.common.C.TIME_UNSET) {
                        _duration.value = controller.duration
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    fun connect() {
        if (controllerFuture != null) return
        Log.d("MusicApp", "MusicServiceConnection: Connecting to service...")

        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture?.addListener({
            Log.d("MusicApp", "MusicServiceConnection: Controller future listener triggered")
            try {
                mediaController = controllerFuture?.get()
                mediaController?.addListener(playerListener)
                _isConnected.value = true
                Log.d("MusicApp", "MusicServiceConnection: Successfully connected to MediaController")
            } catch (e: Exception) {
                Log.e("MusicApp", "MusicServiceConnection: Failed to get MediaController", e)
            }
        }, MoreExecutors.directExecutor())
    }

    fun disconnect() {
        stopPositionUpdates()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        mediaController = null
        _isConnected.value = false
    }

    fun playSong(song: Song) {
        val mediaItem = song.toMediaItem()
        mediaController?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    fun playSongAtPosition(song: Song, positionMs: Long) {
        val controller = mediaController ?: return
        val current = _currentSong.value
        if (current?.id == song.id) {
            controller.seekTo(positionMs)
            controller.play()
        } else {
            val mediaItem = song.toMediaItem()
            controller.setMediaItem(mediaItem, positionMs)
            controller.prepare()
            controller.play()
        }
        _currentPosition.value = positionMs
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        Log.d("MusicApp", "MusicServiceConnection: playSongs called with ${songs.size} songs, index $startIndex")
        if (mediaController == null) {
            Log.e("MusicApp", "MusicServiceConnection: mediaController is NULL! Playback aborted.")
            return
        }
        val mediaItems = songs.map { it.toMediaItem() }
        mediaController?.apply {
            setMediaItems(mediaItems, startIndex, 0L)
            prepare()
            play()
            Log.d("MusicApp", "MusicServiceConnection: setMediaItems and play called")
        }
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun togglePlayPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        _currentPosition.value = position // Update immediately for UI responsiveness
    }

    fun next() {
        mediaController?.seekToNextMediaItem()
    }

    fun previous() {
        mediaController?.seekToPreviousMediaItem()
    }

    fun toggleShuffle() {
        mediaController?.let {
            it.shuffleModeEnabled = !it.shuffleModeEnabled
        }
    }

    fun toggleRepeat() {
        mediaController?.let {
            it.repeatMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    fun saveState() {
        Log.d("MusicApp", "MusicServiceConnection: Requesting SAVE_STATE...")
        mediaController?.sendCustomCommand(
            SessionCommand(MusicService.SAVE_STATE_COMMAND, android.os.Bundle.EMPTY),
            android.os.Bundle.EMPTY
        )
    }

    fun getCurrentPosition(): Long {
        return mediaController?.currentPosition ?: 0L
    }
}
