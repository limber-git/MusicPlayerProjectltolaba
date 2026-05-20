package com.limbe.hexamusicplayer.infrastructure.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.limbe.hexamusicplayer.domain.model.DarkModeMode
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
        val FAVORITE_TRACKS = stringSetPreferencesKey("favorite_tracks")
        val RECENT_TRACKS = stringPreferencesKey("recent_tracks")
        val DARK_MODE = stringPreferencesKey("dark_mode")
        val AUDIO_EFFECTS_ENABLED = booleanPreferencesKey("audio_effects_enabled")
    }

    override val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            playbackSpeed = prefs[Keys.PLAYBACK_SPEED] ?: 1.0f,
            playbackPitch = prefs[Keys.PLAYBACK_PITCH] ?: 1.0f,
            bassStrength = prefs[Keys.BASS_STRENGTH] ?: 0,
            virtualizerStrength = prefs[Keys.VIRTUALIZER_STRENGTH] ?: 0,
            loudnessGainMb = prefs[Keys.LOUDNESS_GAIN_MB] ?: 0,
            eqBandLevels = parseEqBands(prefs[Keys.EQ_BANDS] ?: ""),
            favoriteTrackIds = prefs[Keys.FAVORITE_TRACKS]?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet(),
            recentTrackIds = parseTrackIds(prefs[Keys.RECENT_TRACKS] ?: ""),
            darkModeMode = DarkModeMode.valueOf(prefs[Keys.DARK_MODE] ?: DarkModeMode.SYSTEM.name),
            audioEffectsEnabled = prefs[Keys.AUDIO_EFFECTS_ENABLED] ?: true
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

    override suspend fun toggleFavoriteTrack(trackId: Long) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_TRACKS]?.toMutableSet() ?: mutableSetOf()
            val idString = trackId.toString()
            if (current.contains(idString)) {
                current.remove(idString)
            } else {
                current.add(idString)
            }
            prefs[Keys.FAVORITE_TRACKS] = current
        }
    }

    override suspend fun recordRecentTrack(trackId: Long) {
        context.dataStore.edit { prefs ->
            val updated = (listOf(trackId) + parseTrackIds(prefs[Keys.RECENT_TRACKS] ?: ""))
                .distinct()
                .take(MAX_RECENT_TRACKS)
            prefs[Keys.RECENT_TRACKS] = updated.joinToString(",")
        }
    }

    override suspend fun setDarkModeMode(mode: DarkModeMode) {
        context.dataStore.edit { it[Keys.DARK_MODE] = mode.name }
    }

    override suspend fun setAudioEffectsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUDIO_EFFECTS_ENABLED] = enabled }
    }

    private fun parseEqBands(encoded: String): Map<Int, Int> {
        if (encoded.isBlank()) return emptyMap()
        return encoded.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) {
                parts[0].toIntOrNull()?.let { index ->
                    parts[1].toIntOrNull()?.let { level -> index to level }
                }
            } else {
                null
            }
        }.toMap()
    }

    private fun parseTrackIds(encoded: String): List<Long> {
        if (encoded.isBlank()) return emptyList()
        return encoded.split(",").mapNotNull { it.toLongOrNull() }
    }

    private companion object {
        const val MAX_RECENT_TRACKS = 24
    }
}
