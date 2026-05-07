package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.port.UserPreferencesPort

class ToggleFavoriteTrackUseCase(private val userPreferencesPort: UserPreferencesPort) {
    suspend operator fun invoke(trackId: Long) = userPreferencesPort.toggleFavoriteTrack(trackId)
}
