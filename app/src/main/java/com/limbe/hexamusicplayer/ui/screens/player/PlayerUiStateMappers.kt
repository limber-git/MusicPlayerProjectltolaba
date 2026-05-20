package com.limbe.hexamusicplayer.ui.screens.player

import com.limbe.hexamusicplayer.domain.model.AudioEffectsState
import com.limbe.hexamusicplayer.domain.model.PlayerState
import com.limbe.hexamusicplayer.domain.model.UserPreferences

internal fun PlayerUiState.withPreferences(
    preferences: UserPreferences
): PlayerUiState {
    val currentTrackId = currentTrack?.id
    return copy(
        isFavorite = currentTrackId != null && preferences.favoriteTrackIds.contains(currentTrackId),
        favoriteCount = preferences.favoriteTrackIds.size,
        recentTrackIds = preferences.recentTrackIds,
        darkModeMode = preferences.darkModeMode,
        audioEffectsEnabled = preferences.audioEffectsEnabled,
        appLanguage = preferences.appLanguage,
        manualLibraryFolderUri = preferences.manualLibraryFolderUri,
        manualLibraryFolderLabel = preferences.manualLibraryFolderLabel
    )
}

internal fun PlayerUiState.withPlayerState(
    playerState: PlayerState,
    preferences: UserPreferences
): PlayerUiState {
    return copy(
        currentTrack = playerState.currentTrack,
        queue = playerState.queue,
        isPlaying = playerState.isPlaying,
        currentPositionMs = playerState.positionMs,
        durationMs = playerState.durationMs,
        speed = playerState.speed,
        pitch = playerState.pitch,
        shuffleModeEnabled = playerState.shuffleModeEnabled,
        repeatMode = playerState.repeatMode,
        playerErrorMessage = playerState.errorMessage,
        isFavorite = playerState.currentTrack?.id?.let { id ->
            preferences.favoriteTrackIds.contains(id)
        } ?: false
    )
}

internal fun PlayerUiState.withAudioEffects(
    effectsState: AudioEffectsState
): PlayerUiState {
    return copy(
        attachedSessionId = effectsState.attachedSessionId,
        eqBands = effectsState.bands,
        bassStrength = effectsState.bassStrength,
        virtualizerStrength = effectsState.virtualizerStrength,
        loudnessGainMb = effectsState.loudnessGainMb,
        effectsAvailable = effectsState.effectsAvailable
    )
}
