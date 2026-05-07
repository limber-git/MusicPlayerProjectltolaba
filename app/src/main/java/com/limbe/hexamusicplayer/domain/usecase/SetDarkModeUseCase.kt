package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.model.DarkModeMode
import com.limbe.hexamusicplayer.domain.port.UserPreferencesPort

class SetDarkModeUseCase(private val userPreferencesPort: UserPreferencesPort) {
    suspend operator fun invoke(mode: DarkModeMode) = userPreferencesPort.setDarkModeMode(mode)
}
