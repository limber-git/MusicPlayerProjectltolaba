package com.limbe.hexamusicplayer.domain.model

data class MusicAnalysisState(
    val trackId: Long? = null,
    val status: MusicAnalysisStatus = MusicAnalysisStatus.NOT_ANALYZED,
    val keyEstimate: KeyEstimate? = null,
    val currentNote: DetectedNote? = null,
    val chordEvents: List<ChordEvent> = emptyList(),
    val selectedInstrument: StudioInstrument = StudioInstrument.KEYBOARD,
    val instrumentViews: List<InstrumentView> = defaultInstrumentViews(),
    val errorMessage: String? = null
) {
    val hasManualData: Boolean
        get() = keyEstimate != null || chordEvents.isNotEmpty() || currentNote != null
}

data class KeyEstimate(
    val tonic: String,
    val mode: KeyMode,
    val confidence: Float,
    val source: AnalysisSource
) {
    val displayName: String
        get() = "$tonic ${mode.displayName}"
}

enum class KeyMode(val displayName: String) {
    MAJOR("major"),
    MINOR("minor"),
    UNKNOWN("")
}

data class DetectedNote(
    val noteName: String,
    val octave: Int? = null,
    val startMs: Long,
    val confidence: Float,
    val source: AnalysisSource
) {
    val displayName: String
        get() = if (octave == null) noteName else "$noteName$octave"
}

data class ChordEvent(
    val startMs: Long,
    val endMs: Long? = null,
    val chordName: String,
    val confidence: Float,
    val source: AnalysisSource
)

enum class AnalysisSource {
    MANUAL,
    ESTIMATED,
    IMPORTED
}

enum class StudioInstrument(val displayName: String) {
    KEYBOARD("Keyboard"),
    GUITAR("Guitar"),
    BASS("Bass"),
    SAX("Sax"),
    FLUTE("Flute")
}

data class InstrumentView(
    val instrument: StudioInstrument,
    val title: String,
    val body: String
)

fun defaultInstrumentViews(): List<InstrumentView> = listOf(
    InstrumentView(
        instrument = StudioInstrument.KEYBOARD,
        title = StudioInstrument.KEYBOARD.displayName,
        body = "Use the chord timeline as voicings and melody targets."
    ),
    InstrumentView(
        instrument = StudioInstrument.GUITAR,
        title = StudioInstrument.GUITAR.displayName,
        body = "Map chord events to fretboard shapes and capo/transposition notes."
    ),
    InstrumentView(
        instrument = StudioInstrument.BASS,
        title = StudioInstrument.BASS.displayName,
        body = "Follow roots first, then add fifths and approach notes."
    ),
    InstrumentView(
        instrument = StudioInstrument.SAX,
        title = StudioInstrument.SAX.displayName,
        body = "Use key and chord tones as safe notes for phrases."
    ),
    InstrumentView(
        instrument = StudioInstrument.FLUTE,
        title = StudioInstrument.FLUTE.displayName,
        body = "Use key tones and chord changes for melodic practice."
    )
)
