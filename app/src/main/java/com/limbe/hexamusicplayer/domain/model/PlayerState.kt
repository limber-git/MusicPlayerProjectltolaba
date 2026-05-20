package com.limbe.hexamusicplayer.domain.model

data class PlayerState(
    val currentTrack: Track? = null,
    val queue: List<Track> = emptyList(),
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val audioSessionId: Int? = null,
    val shuffleModeEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val errorMessage: String? = null
)

enum class RepeatMode {
    OFF, ONE, ALL
}
