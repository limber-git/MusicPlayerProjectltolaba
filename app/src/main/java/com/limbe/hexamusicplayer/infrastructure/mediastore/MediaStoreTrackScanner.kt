package com.limbe.hexamusicplayer.infrastructure.mediastore

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.limbe.hexamusicplayer.domain.model.Track

class MediaStoreTrackScanner(
    private val context: Context
) {

    fun scan(): List<Track> {
        val tracks = mutableListOf<Track>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val sourcePathColumnName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.RELATIVE_PATH
        } else {
            MediaStore.Audio.Media.DATA
        }
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.IS_MUSIC,
            sourcePathColumnName
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
            val sourcePathColumn = cursor.getColumnIndexOrThrow(sourcePathColumnName)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Untitled"
                val artist = cursor.getString(artistColumn) ?: "Unknown artist"
                val album = cursor.getString(albumColumn) ?: "Unknown album"
                val albumId = cursor.getLong(albumIdColumn)
                val durationMs = cursor.getLong(durationColumn).coerceAtLeast(0L)
                val sourcePath = cursor.getString(sourcePathColumn)
                val uri = ContentUris.withAppendedId(collection, id).toString()

                tracks += Track(
                    id = id,
                    title = title,
                    artist = artist,
                    album = album,
                    albumId = albumId.takeIf { it > 0L },
                    durationMs = durationMs,
                    contentUri = uri,
                    artworkUri = albumArtworkUri(albumId),
                    sourcePath = sourcePath
                )
            }
        }

        return tracks
    }

    private fun albumArtworkUri(albumId: Long): String? {
        if (albumId <= 0L) return null

        return ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        ).toString()
    }
}
