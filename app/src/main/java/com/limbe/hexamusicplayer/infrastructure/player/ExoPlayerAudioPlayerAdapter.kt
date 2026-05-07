package com.limbe.hexamusicplayer.infrastructure.player

import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
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

    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val progressLoop = object : Runnable {
        override fun run() {
            publishState()
            mainHandler.postDelayed(this, 500L)
        }
    }

    init {
        exoPlayer.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    publishState()
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
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
                    val currentId = mediaItem?.mediaId?.toLongOrNull()
                    val currentTrack = currentQueue.find { it.id == currentId }
                    if (currentTrack != null) {
                        _state.update { it.copy(currentTrack = currentTrack) }
                    }
                    publishState()
                }

                override fun onEvents(player: Player, events: Player.Events) {
                    publishState()
                }
            }
        )
        mainHandler.post(progressLoop)
    }

    override fun play(track: Track, queue: List<Track>) {
        currentQueue = queue.ifEmpty { listOf(track) }
        
        val mediaItems = currentQueue.map { t ->
            MediaItem.Builder()
                .setUri(Uri.parse(t.contentUri))
                .setMediaId(t.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(t.title)
                        .setArtist(t.artist)
                        .build()
                )
                .build()
        }

        val startIndex = currentQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        
        exoPlayer.setMediaItems(mediaItems, startIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        _state.update { current ->
            current.copy(currentTrack = track)
        }
        publishState()
    }

    override fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
        publishState()
    }

    override fun seekTo(positionMs: Long) {
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
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNext()
        }
    }

    override fun skipToPrevious() {
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

    override fun release() {
        mainHandler.removeCallbacks(progressLoop)
        exoPlayer.release()
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
                }
            )
        }
    }
}
