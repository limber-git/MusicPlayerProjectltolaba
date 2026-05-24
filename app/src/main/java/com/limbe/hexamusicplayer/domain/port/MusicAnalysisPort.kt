package com.limbe.hexamusicplayer.domain.port

import com.limbe.hexamusicplayer.domain.model.ChordEvent
import com.limbe.hexamusicplayer.domain.model.KeyEstimate
import com.limbe.hexamusicplayer.domain.model.MusicAnalysisState
import com.limbe.hexamusicplayer.domain.model.StudioInstrument
import com.limbe.hexamusicplayer.domain.model.Track
import kotlinx.coroutines.flow.StateFlow

interface MusicAnalysisPort {
    val state: StateFlow<MusicAnalysisState>

    suspend fun loadTrack(track: Track?)
    suspend fun analyzeTrack(track: Track)
    suspend fun saveManualKey(track: Track, keyEstimate: KeyEstimate)
    suspend fun addManualChord(track: Track, chordEvent: ChordEvent)
    fun selectInstrument(instrument: StudioInstrument)
}
