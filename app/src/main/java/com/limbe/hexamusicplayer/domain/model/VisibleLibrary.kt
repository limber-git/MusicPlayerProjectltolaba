package com.limbe.hexamusicplayer.domain.model

data class VisibleLibrary(
    val visibleTracks: List<Track>,
    val filteredTracks: List<Track>,
    val favoriteTracks: List<Track>,
    val recentTracks: List<Track>,
    val activeFolderLabel: String?
)
