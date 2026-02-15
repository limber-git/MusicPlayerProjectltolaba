package com.limbe.hexamusicplayer.domain.port

import com.limbe.hexamusicplayer.domain.model.PlayerState
import com.limbe.hexamusicplayer.domain.model.Track
import kotlinx.coroutines.flow.StateFlow

interface AudioPlayerPort {
    val state: StateFlow<PlayerState>

    fun play(track: Track)
    fun togglePlayPause()
    fun seekTo(positionMs: Long)
    fun setSpeed(speed: Float)
    fun setPitch(pitch: Float)
    fun release()
}
