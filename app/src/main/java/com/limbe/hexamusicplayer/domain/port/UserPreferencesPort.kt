package com.limbe.hexamusicplayer.domain.port

import com.limbe.hexamusicplayer.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesPort {
    val preferences: Flow<UserPreferences>
    suspend fun setPlaybackSpeed(speed: Float)
    suspend fun setPlaybackPitch(pitch: Float)
    suspend fun setBassStrength(strength: Int)
    suspend fun setVirtualizerStrength(strength: Int)
    suspend fun setLoudnessGainMb(gainMb: Int)
    suspend fun setEqBandLevel(index: Int, level: Int)
    suspend fun toggleFavoriteTrack(trackId: Long)
    suspend fun recordRecentTrack(trackId: Long)
    suspend fun setDarkModeMode(mode: com.limbe.hexamusicplayer.domain.model.DarkModeMode)
    suspend fun setAudioEffectsEnabled(enabled: Boolean)
}
