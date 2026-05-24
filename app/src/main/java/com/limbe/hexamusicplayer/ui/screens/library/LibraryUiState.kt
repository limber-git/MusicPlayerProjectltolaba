package com.limbe.hexamusicplayer.ui.screens.library

import com.limbe.hexamusicplayer.domain.model.Track

data class LibraryUiState(
    val tracks: List<Track> = emptyList(),
    val filteredTracks: List<Track> = emptyList(),
    val favoriteTracks: List<Track> = emptyList(),
    val recentTracks: List<Track> = emptyList(),
    val activeLibraryFolderLabel: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val hasLoadedOnce: Boolean = false,
    val errorMessage: String? = null
)
