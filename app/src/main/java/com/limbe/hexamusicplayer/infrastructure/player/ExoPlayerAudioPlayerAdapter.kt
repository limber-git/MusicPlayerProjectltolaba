package com.limbe.hexamusicplayer.infrastructure.player

import android.os.Handler
import android.os.Looper
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.limbe.hexamusicplayer.domain.model.PlayerState
import com.limbe.hexamusicplayer.domain.model.RepeatMode
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.port.AudioPlayerPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@UnstableApi
class ExoPlayerAudioPlayerAdapter(
    private val exoPlayer: ExoPlayer
) : AudioPlayerPort {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentQueue: List<Track> = emptyList()
    private var lastErrorMessage: String? = null
    private var progressLoopScheduled = false

    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val progressLoop = object : Runnable {
        override fun run() {
            publishState()
            if (shouldKeepProgressLoopRunning()) {
                mainHandler.postDelayed(this, 500L)
            } else {
                progressLoopScheduled = false
            }
        }
    }

    init {
        exoPlayer.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        lastErrorMessage = null
                    }
                    syncProgressLoop()
                    publishState()
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    syncProgressLoop()
                    publishState()
                }

                override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                    publishState()
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    publishState()
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    publishState()
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    lastErrorMessage = null
                    val currentId = mediaItem?.mediaId?.toLongOrNull()
                    val currentTrack = currentQueue.find { it.id == currentId }
                    if (currentTrack != null) {
                        _state.update { it.copy(currentTrack = currentTrack, errorMessage = null) }
                    }
                    syncProgressLoop()
                    publishState()
                }

                override fun onPlayerError(error: PlaybackException) {
                    lastErrorMessage = error.localizedMessage ?: "Playback error"
                    syncProgressLoop()
                    publishState()
                }

                override fun onEvents(player: Player, events: Player.Events) {
                    syncProgressLoop()
                    publishState()
                }
            }
        )
        publishState()
    }

    override fun play(track: Track, queue: List<Track>) {
        currentQueue = queue.ifEmpty { listOf(track) }
        lastErrorMessage = null

        val mediaItems = currentQueue.map { t ->
            MediaItem.Builder()
                .setUri(t.contentUri.toUri())
                .setMediaId(t.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(t.title)
                        .setArtist(t.artist)
                        .setAlbumTitle(t.album)
                        .setArtworkUri(t.artworkUri?.toUri())
                        .build()
                )
                .build()
        }

        val startIndex = currentQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

        exoPlayer.setMediaItems(mediaItems, startIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        syncProgressLoop()

        _state.update { current ->
            current.copy(currentTrack = track, errorMessage = null)
        }
        publishState()
    }

    override fun togglePlayPause() {
        lastErrorMessage = null
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
        syncProgressLoop()
        publishState()
    }

    override fun seekTo(positionMs: Long) {
        lastErrorMessage = null
        exoPlayer.seekTo(positionMs)
        publishState()
    }

    override fun setSpeed(speed: Float) {
        val current = exoPlayer.playbackParameters
        exoPlayer.playbackParameters = PlaybackParameters(speed, current.pitch)
        publishState()
    }

    override fun setPitch(pitch: Float) {
        val current = exoPlayer.playbackParameters
        exoPlayer.playbackParameters = PlaybackParameters(current.speed, pitch)
        publishState()
    }

    override fun skipToNext() {
        lastErrorMessage = null
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNext()
        }
    }

    override fun skipToPrevious() {
        lastErrorMessage = null
        if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPrevious()
        }
    }

    override fun setShuffleMode(enabled: Boolean) {
        exoPlayer.shuffleModeEnabled = enabled
        publishState()
    }

    override fun setRepeatMode(mode: RepeatMode) {
        exoPlayer.repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
        publishState()
    }

    override fun addToQueue(track: Track) {
        currentQueue = currentQueue + track
        exoPlayer.addMediaItem(track.toMediaItem())
        publishState()
    }

    override fun playNext(track: Track) {
        if (currentQueue.isEmpty()) {
            play(track, listOf(track))
            return
        }

        val insertIndex = (exoPlayer.currentMediaItemIndex + 1).coerceAtLeast(0)
        currentQueue = currentQueue.toMutableList().apply {
            add(insertIndex.coerceAtMost(size), track)
        }
        exoPlayer.addMediaItem(insertIndex, track.toMediaItem())
        publishState()
    }

    override fun removeFromQueue(trackId: Long) {
        val index = currentQueue.indexOfFirst { it.id == trackId }
        if (index < 0) return

        val removingCurrent = exoPlayer.currentMediaItemIndex == index
        currentQueue = currentQueue.toMutableList().apply { removeAt(index) }
        exoPlayer.removeMediaItem(index)

        if (currentQueue.isEmpty()) {
            exoPlayer.stop()
        } else if (removingCurrent) {
            exoPlayer.playWhenReady = true
        }
        publishState()
    }

    override fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        if (fromIndex !in currentQueue.indices || toIndex !in currentQueue.indices) return

        val mutableQueue = currentQueue.toMutableList()
        val movedTrack = mutableQueue.removeAt(fromIndex)
        mutableQueue.add(toIndex, movedTrack)
        currentQueue = mutableQueue
        exoPlayer.moveMediaItem(fromIndex, toIndex)
        publishState()
    }

    override fun release() {
        mainHandler.removeCallbacks(progressLoop)
        progressLoopScheduled = false
        exoPlayer.release()
    }

    private fun syncProgressLoop() {
        if (shouldKeepProgressLoopRunning()) {
            if (!progressLoopScheduled) {
                progressLoopScheduled = true
                mainHandler.post(progressLoop)
            }
        } else if (progressLoopScheduled) {
            mainHandler.removeCallbacks(progressLoop)
            progressLoopScheduled = false
        }
    }

    private fun shouldKeepProgressLoopRunning(): Boolean {
        return exoPlayer.isPlaying || exoPlayer.playbackState == Player.STATE_BUFFERING
    }

    private fun publishState() {
        val duration = if (exoPlayer.duration == C.TIME_UNSET) 0L else exoPlayer.duration
        val audioSession = exoPlayer.audioSessionId
        val normalizedSession = if (audioSession == C.AUDIO_SESSION_ID_UNSET) null else audioSession

        _state.update { current ->
            current.copy(
                isPlaying = exoPlayer.isPlaying,
                positionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                durationMs = duration.coerceAtLeast(0L),
                speed = exoPlayer.playbackParameters.speed,
                pitch = exoPlayer.playbackParameters.pitch,
                audioSessionId = normalizedSession,
                shuffleModeEnabled = exoPlayer.shuffleModeEnabled,
                repeatMode = when (exoPlayer.repeatMode) {
                    Player.REPEAT_MODE_OFF -> RepeatMode.OFF
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                },
                queue = currentQueue,
                errorMessage = lastErrorMessage
            )
        }
    }

    private fun Track.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setUri(contentUri.toUri())
            .setMediaId(id.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                .setArtworkUri(artworkUri?.toUri())
                    .build()
            )
            .build()
    }
}
