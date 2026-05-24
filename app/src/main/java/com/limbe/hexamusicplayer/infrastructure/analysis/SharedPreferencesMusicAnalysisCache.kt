package com.limbe.hexamusicplayer.infrastructure.analysis

import android.content.Context
import android.net.Uri
import com.limbe.hexamusicplayer.domain.model.AnalysisSource
import com.limbe.hexamusicplayer.domain.model.ChordEvent
import com.limbe.hexamusicplayer.domain.model.DetectedNote
import com.limbe.hexamusicplayer.domain.model.KeyEstimate
import com.limbe.hexamusicplayer.domain.model.KeyMode
import com.limbe.hexamusicplayer.domain.model.MusicAnalysisState
import com.limbe.hexamusicplayer.domain.model.MusicAnalysisStatus
import com.limbe.hexamusicplayer.domain.model.StudioInstrument
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.port.MusicAnalysisCachePort

class SharedPreferencesMusicAnalysisCache(context: Context) : MusicAnalysisCachePort {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override suspend fun load(track: Track): MusicAnalysisState? {
        val encoded = preferences.getString(keyFor(track), null) ?: return null
        return decode(track.id, encoded)
    }

    override suspend fun save(track: Track, analysis: MusicAnalysisState) {
        preferences.edit()
            .putString(keyFor(track), encode(analysis.copy(trackId = track.id)))
            .apply()
    }

    private fun keyFor(track: Track): String = "track_${track.id}"

    private fun encode(analysis: MusicAnalysisState): String {
        val key = analysis.keyEstimate?.let {
            listOf(
                encodePart(it.tonic),
                it.mode.name,
                it.confidence.toString(),
                it.source.name
            ).joinToString(":")
        }.orEmpty()

        val chords = analysis.chordEvents.joinToString(";") {
            listOf(
                it.startMs.toString(),
                it.endMs?.toString().orEmpty(),
                encodePart(it.chordName),
                it.confidence.toString(),
                it.source.name
            ).joinToString(":")
        }

        val note = analysis.currentNote?.let {
            listOf(
                encodePart(it.noteName),
                it.octave?.toString().orEmpty(),
                it.startMs.toString(),
                it.confidence.toString(),
                it.source.name
            ).joinToString(":")
        }.orEmpty()

        return listOf(
            analysis.status.name,
            analysis.selectedInstrument.name,
            key,
            chords,
            note
        ).joinToString("|")
    }

    private fun decode(trackId: Long, encoded: String): MusicAnalysisState? {
        val parts = encoded.split("|", limit = 5)
        if (parts.size < 4) return null

        val status = parts[0].toEnumOrDefault(MusicAnalysisStatus.MANUAL)
        val selectedInstrument = parts[1].toEnumOrDefault(StudioInstrument.KEYBOARD)
        val keyEstimate = decodeKey(parts[2])
        val chordEvents = decodeChords(parts[3])
        val currentNote = parts.getOrNull(4)?.let(::decodeNote)

        return MusicAnalysisState(
            trackId = trackId,
            status = status,
            keyEstimate = keyEstimate,
            currentNote = currentNote,
            chordEvents = chordEvents,
            selectedInstrument = selectedInstrument
        )
    }

    private fun decodeKey(encoded: String): KeyEstimate? {
        if (encoded.isBlank()) return null
        val parts = encoded.split(":", limit = 4)
        if (parts.size < 4) return null

        return KeyEstimate(
            tonic = decodePart(parts[0]),
            mode = parts[1].toEnumOrDefault(KeyMode.UNKNOWN),
            confidence = parts[2].toFloatOrNull() ?: 1f,
            source = parts[3].toEnumOrDefault(AnalysisSource.MANUAL)
        )
    }

    private fun decodeNote(encoded: String): DetectedNote? {
        if (encoded.isBlank()) return null
        val parts = encoded.split(":", limit = 5)
        if (parts.size < 5) return null

        return DetectedNote(
            noteName = decodePart(parts[0]),
            octave = parts[1].toIntOrNull(),
            startMs = parts[2].toLongOrNull() ?: return null,
            confidence = parts[3].toFloatOrNull() ?: 1f,
            source = parts[4].toEnumOrDefault(AnalysisSource.ESTIMATED)
        )
    }

    private fun decodeChords(encoded: String): List<ChordEvent> {
        if (encoded.isBlank()) return emptyList()
        return encoded.split(";").mapNotNull { chord ->
            val parts = chord.split(":", limit = 5)
            if (parts.size < 5) return@mapNotNull null

            ChordEvent(
                startMs = parts[0].toLongOrNull() ?: return@mapNotNull null,
                endMs = parts[1].toLongOrNull(),
                chordName = decodePart(parts[2]),
                confidence = parts[3].toFloatOrNull() ?: 1f,
                source = parts[4].toEnumOrDefault(AnalysisSource.MANUAL)
            )
        }.sortedBy { it.startMs }
    }

    private fun encodePart(value: String): String = Uri.encode(value)

    private fun decodePart(value: String): String = Uri.decode(value)

    private inline fun <reified T : Enum<T>> String.toEnumOrDefault(default: T): T {
        return runCatching { enumValueOf<T>(this) }.getOrDefault(default)
    }

    private companion object {
        const val PREFERENCES_NAME = "music_analysis_cache"
    }
}
