package com.limbe.hexamusicplayer.infrastructure.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.limbe.hexamusicplayer.domain.model.UserPreferences
import com.limbe.hexamusicplayer.domain.port.UserPreferencesPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_settings")

class DataStoreUserPreferencesAdapter(private val context: Context) : UserPreferencesPort {

    private object Keys {
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val PLAYBACK_PITCH = floatPreferencesKey("playback_pitch")
        val BASS_STRENGTH = intPreferencesKey("bass_strength")
        val VIRTUALIZER_STRENGTH = intPreferencesKey("virtualizer_strength")
        val LOUDNESS_GAIN_MB = intPreferencesKey("loudness_gain_mb")
        val EQ_BANDS = stringPreferencesKey("eq_bands")
    }

    override val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            playbackSpeed = prefs[Keys.PLAYBACK_SPEED] ?: 1.0f,
            playbackPitch = prefs[Keys.PLAYBACK_PITCH] ?: 1.0f,
            bassStrength = prefs[Keys.BASS_STRENGTH] ?: 0,
            virtualizerStrength = prefs[Keys.VIRTUALIZER_STRENGTH] ?: 0,
            loudnessGainMb = prefs[Keys.LOUDNESS_GAIN_MB] ?: 0,
            eqBandLevels = parseEqBands(prefs[Keys.EQ_BANDS] ?: "")
        )
    }

    override suspend fun setPlaybackSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.PLAYBACK_SPEED] = speed }
    }

    override suspend fun setPlaybackPitch(pitch: Float) {
        context.dataStore.edit { it[Keys.PLAYBACK_PITCH] = pitch }
    }

    override suspend fun setBassStrength(strength: Int) {
        context.dataStore.edit { it[Keys.BASS_STRENGTH] = strength }
    }

    override suspend fun setVirtualizerStrength(strength: Int) {
        context.dataStore.edit { it[Keys.VIRTUALIZER_STRENGTH] = strength }
    }

    override suspend fun setLoudnessGainMb(gainMb: Int) {
        context.dataStore.edit { it[Keys.LOUDNESS_GAIN_MB] = gainMb }
    }

    override suspend fun setEqBandLevel(index: Int, level: Int) {
        context.dataStore.edit { prefs ->
            val currentMap = parseEqBands(prefs[Keys.EQ_BANDS] ?: "").toMutableMap()
            currentMap[index] = level
            prefs[Keys.EQ_BANDS] = currentMap.entries.joinToString(",") { "${it.key}:${it.value}" }
        }
    }

    private fun parseEqBands(encoded: String): Map<Int, Int> {
        if (encoded.isBlank()) return emptyMap()
        return encoded.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) {
                parts[0].toIntOrNull()?.let { index ->
                    parts[1].toIntOrNull()?.let { level -> index to level }
                }
            } else null
        }.toMap()
    }
}
