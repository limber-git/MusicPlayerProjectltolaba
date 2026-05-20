package com.limbe.hexamusicplayer.infrastructure.mediastore

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.port.LocalMusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreLocalMusicRepository(
    private val context: Context
) : LocalMusicRepository {

    override suspend fun listLocalTracks(): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.IS_MUSIC
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Untitled"
                val artist = cursor.getString(artistColumn) ?: "Unknown artist"
                val album = cursor.getString(albumColumn) ?: "Unknown album"
                val albumId = cursor.getLong(albumIdColumn)
                val durationMs = cursor.getLong(durationColumn).coerceAtLeast(0L)
                val uri = ContentUris.withAppendedId(collection, id).toString()
                val artworkUri = albumArtworkUri(albumId)

                tracks += Track(
                    id = id,
                    title = title,
                    artist = artist,
                    album = album,
                    albumId = albumId.takeIf { it > 0L },
                    durationMs = durationMs,
                    contentUri = uri,
                    artworkUri = artworkUri
                )
            }
        }

        tracks
    }

    private fun albumArtworkUri(albumId: Long): String? {
        if (albumId <= 0L) return null

        return ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        ).toString()
    }
}
