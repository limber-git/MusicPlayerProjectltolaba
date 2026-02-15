package com.limbe.hexamusicplayer.domain.model

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val contentUri: String
)
