package com.limbe.hexamusicplayer.infrastructure.mediastore

import android.content.Context
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.port.LocalMusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreLocalMusicRepository(
    context: Context
) : LocalMusicRepository {

    private val scanner = MediaStoreTrackScanner(context)

    override suspend fun listLocalTracks(): List<Track> = withContext(Dispatchers.IO) {
        scanner.scan()
    }
}
