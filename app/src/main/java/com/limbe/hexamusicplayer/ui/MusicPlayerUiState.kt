package com.limbe.hexamusicplayer.ui

import com.limbe.hexamusicplayer.domain.model.EqBand
import com.limbe.hexamusicplayer.domain.model.Track

data class MusicPlayerUiState(
    val tracks: List<Track> = emptyList(),
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
    val loudnessGainMb: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
