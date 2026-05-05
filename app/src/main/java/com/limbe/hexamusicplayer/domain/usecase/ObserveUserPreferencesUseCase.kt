package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.model.UserPreferences
import com.limbe.hexamusicplayer.domain.port.UserPreferencesPort
import kotlinx.coroutines.flow.Flow

class ObserveUserPreferencesUseCase(
    private val preferencesPort: UserPreferencesPort
) {
    operator fun invoke(): Flow<UserPreferences> = preferencesPort.preferences
}
