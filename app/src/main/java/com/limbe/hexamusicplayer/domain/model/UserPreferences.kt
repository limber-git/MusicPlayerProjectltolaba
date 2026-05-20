package com.limbe.hexamusicplayer.domain.model

data class UserPreferences(
    val playbackSpeed: Float = 1.0f,
    val playbackPitch: Float = 1.0f,
    val bassStrength: Int = 0,
    val virtualizerStrength: Int = 0,
    val loudnessGainMb: Int = 0,
    val eqBandLevels: Map<Int, Int> = emptyMap(),
    val favoriteTrackIds: Set<Long> = emptySet(),
    val recentTrackIds: List<Long> = emptyList(),
    val darkModeMode: DarkModeMode = DarkModeMode.SYSTEM,
    val audioEffectsEnabled: Boolean = true
)

enum class DarkModeMode {
    SYSTEM, LIGHT, DARK
}
