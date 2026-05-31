package com.limbe.hexamusicplayer.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long?,
    val durationMs: Long,
    val contentUri: String,
    val artworkUri: String? = null,
    val sourcePath: String? = null
)
