package com.limbe.hexamusicplayer.infrastructure.analysis

import com.limbe.hexamusicplayer.domain.model.ChordEvent
import com.limbe.hexamusicplayer.domain.model.KeyEstimate
import com.limbe.hexamusicplayer.domain.model.MusicAnalysisState
import com.limbe.hexamusicplayer.domain.model.MusicAnalysisStatus
import com.limbe.hexamusicplayer.domain.model.StudioInstrument
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.port.MusicAnalysisCachePort
import com.limbe.hexamusicplayer.domain.port.MusicAnalysisPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class ManualMusicAnalysisAdapter(
    private val cachePort: MusicAnalysisCachePort,
    private val pcmAudioExtractor: AndroidPcmAudioExtractor,
    private val musicAnalyzer: LightweightMusicAnalyzer
) : MusicAnalysisPort {

    private val _state = MutableStateFlow(MusicAnalysisState())
    override val state: StateFlow<MusicAnalysisState> = _state.asStateFlow()

    override suspend fun loadTrack(track: Track?) {
        if (track == null) {
            _state.value = MusicAnalysisState()
            return
        }

        _state.value = cachePort.load(track)
            ?: MusicAnalysisState(
                trackId = track.id,
                status = MusicAnalysisStatus.NOT_ANALYZED
            )
    }

    override suspend fun analyzeTrack(track: Track) {
        val previous = _state.value.takeIf { it.trackId == track.id }
        _state.value = (previous ?: MusicAnalysisState(trackId = track.id)).copy(
            status = MusicAnalysisStatus.ANALYZING,
            errorMessage = null
        )

        val result = runCatching {
            withContext(Dispatchers.Default) {
                val pcm = pcmAudioExtractor.extract(track)
                musicAnalyzer.analyze(pcm, track.id)
            }
        }

        result
            .onSuccess { estimated ->
                val merged = mergeWithManualData(
                    previous = previous,
                    estimated = estimated.copy(selectedInstrument = _state.value.selectedInstrument)
                )
                _state.value = merged
                cachePort.save(track, merged)
            }
            .onFailure { throwable ->
                _state.value = (previous ?: MusicAnalysisState(trackId = track.id)).copy(
                    status = MusicAnalysisStatus.FAILED,
                    errorMessage = throwable.localizedMessage ?: "Analysis failed"
                )
            }
    }

    override suspend fun saveManualKey(track: Track, keyEstimate: KeyEstimate) {
        val updated = _state.value.copy(
            trackId = track.id,
            status = MusicAnalysisStatus.MANUAL,
            keyEstimate = keyEstimate,
            errorMessage = null
        )
        _state.value = updated
        cachePort.save(track, updated)
    }

    override suspend fun addManualChord(track: Track, chordEvent: ChordEvent) {
        val updated = _state.value.copy(
            trackId = track.id,
            status = MusicAnalysisStatus.MANUAL,
            chordEvents = (_state.value.chordEvents + chordEvent).sortedBy { it.startMs },
            errorMessage = null
        )
        _state.value = updated
        cachePort.save(track, updated)
    }

    override fun selectInstrument(instrument: StudioInstrument) {
        _state.value = _state.value.copy(selectedInstrument = instrument)
    }

    private fun mergeWithManualData(
        previous: MusicAnalysisState?,
        estimated: MusicAnalysisState
    ): MusicAnalysisState {
        if (previous == null || previous.status != MusicAnalysisStatus.MANUAL) {
            return estimated
        }

        val key = previous.keyEstimate ?: estimated.keyEstimate
        val chords = previous.chordEvents.ifEmpty { estimated.chordEvents }
        return estimated.copy(
            status = MusicAnalysisStatus.MANUAL,
            keyEstimate = key,
            chordEvents = chords,
            selectedInstrument = previous.selectedInstrument
        )
    }
}
