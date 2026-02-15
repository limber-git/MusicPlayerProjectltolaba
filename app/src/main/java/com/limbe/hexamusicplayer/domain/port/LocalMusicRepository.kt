package com.limbe.hexamusicplayer.domain.port

import com.limbe.hexamusicplayer.domain.model.Track

interface LocalMusicRepository {
    suspend fun listLocalTracks(): List<Track>
}
