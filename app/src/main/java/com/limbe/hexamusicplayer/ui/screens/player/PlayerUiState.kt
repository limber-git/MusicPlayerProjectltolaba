package com.limbe.hexamusicplayer.ui.screens.player

import com.limbe.hexamusicplayer.domain.model.EqBand
import com.limbe.hexamusicplayer.domain.model.Track

data class PlayerUiState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0,
    val durationMs: Long = 0,
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val attachedSessionId: Int? = null,
    val eqBands: List<EqBand> = emptyList(),
    val bassStrength: Int = 0,
    val virtualizerStrength: Int = 0,
    val loudnessGainMb: Int = 0
)
