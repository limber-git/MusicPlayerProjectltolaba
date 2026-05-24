package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.model.ChordEvent
import com.limbe.hexamusicplayer.domain.model.KeyEstimate
import com.limbe.hexamusicplayer.domain.model.MusicAnalysisState
import com.limbe.hexamusicplayer.domain.model.StudioInstrument
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.port.MusicAnalysisPort
import kotlinx.coroutines.flow.StateFlow

class ObserveMusicAnalysisUseCase(
    private val musicAnalysisPort: MusicAnalysisPort
) {
    operator fun invoke(): StateFlow<MusicAnalysisState> = musicAnalysisPort.state
}

class LoadTrackAnalysisUseCase(
    private val musicAnalysisPort: MusicAnalysisPort
) {
    suspend operator fun invoke(track: Track?) = musicAnalysisPort.loadTrack(track)
}

class AnalyzeTrackUseCase(
    private val musicAnalysisPort: MusicAnalysisPort
) {
    suspend operator fun invoke(track: Track) = musicAnalysisPort.analyzeTrack(track)
}

class SaveManualKeyUseCase(
    private val musicAnalysisPort: MusicAnalysisPort
) {
    suspend operator fun invoke(track: Track, keyEstimate: KeyEstimate) {
        musicAnalysisPort.saveManualKey(track, keyEstimate)
    }
}

class AddManualChordUseCase(
    private val musicAnalysisPort: MusicAnalysisPort
) {
    suspend operator fun invoke(track: Track, chordEvent: ChordEvent) {
        musicAnalysisPort.addManualChord(track, chordEvent)
    }
}

class SelectStudioInstrumentUseCase(
    private val musicAnalysisPort: MusicAnalysisPort
) {
    operator fun invoke(instrument: StudioInstrument) {
        musicAnalysisPort.selectInstrument(instrument)
    }
}
