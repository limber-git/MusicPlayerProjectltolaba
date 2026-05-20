package com.limbe.hexamusicplayer.domain.model

data class EqBand(
    val index: Int,
    val centerFreqHz: Int,
    val level: Int,
    val minLevel: Int,
    val maxLevel: Int
)

data class AudioEffectsState(
    val attachedSessionId: Int? = null,
    val bands: List<EqBand> = emptyList(),
    val bassStrength: Int = 0,
    val virtualizerStrength: Int = 0,
    val loudnessGainMb: Int = 0,
    val effectsAvailable: Boolean = false
)
