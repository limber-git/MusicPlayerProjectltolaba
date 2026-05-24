package com.limbe.hexamusicplayer.domain.port

import com.limbe.hexamusicplayer.domain.model.MusicAnalysisState
import com.limbe.hexamusicplayer.domain.model.Track

interface MusicAnalysisCachePort {
    suspend fun load(track: Track): MusicAnalysisState?
    suspend fun save(track: Track, analysis: MusicAnalysisState)
}
