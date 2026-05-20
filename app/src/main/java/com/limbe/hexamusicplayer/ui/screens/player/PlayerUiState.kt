package com.limbe.hexamusicplayer.ui.screens.player

import com.limbe.hexamusicplayer.domain.model.AppLanguage
import com.limbe.hexamusicplayer.domain.model.DarkModeMode
import com.limbe.hexamusicplayer.domain.model.EqBand
import com.limbe.hexamusicplayer.domain.model.RepeatMode
import com.limbe.hexamusicplayer.domain.model.Track

data class PlayerUiState(
    val currentTrack: Track? = null,
    val queue: List<Track> = emptyList(),
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
    val effectsAvailable: Boolean = false,
    val shuffleModeEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isFavorite: Boolean = false,
    val favoriteCount: Int = 0,
    val recentTrackIds: List<Long> = emptyList(),
    val darkModeMode: DarkModeMode = DarkModeMode.SYSTEM,
    val audioEffectsEnabled: Boolean = true,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val manualLibraryFolderUri: String? = null,
    val manualLibraryFolderLabel: String? = null,
    val playerErrorMessage: String? = null
)
