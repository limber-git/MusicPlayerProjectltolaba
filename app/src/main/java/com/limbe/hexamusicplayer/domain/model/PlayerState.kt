package com.limbe.hexamusicplayer.domain.model

data class PlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val audioSessionId: Int? = null
)
