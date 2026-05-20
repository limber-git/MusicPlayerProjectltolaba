package com.limbe.hexamusicplayer.ui.screens.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.model.UserPreferences
import com.limbe.hexamusicplayer.domain.usecase.GetLocalTracksUseCase
import com.limbe.hexamusicplayer.domain.usecase.ObserveUserPreferencesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val getLocalTracksUseCase: GetLocalTracksUseCase,
    private val observeUserPreferencesUseCase: ObserveUserPreferencesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private var currentPreferences = UserPreferences()
    private var allTracks: List<Track> = emptyList()

    init {
        observePreferences()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filteredTracks = filterTracks(state.tracks, query)
            state.copy(
                searchQuery = query,
                filteredTracks = filteredTracks
            )
        }
    }

    fun refreshTracks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { getLocalTracksUseCase() }
                .onSuccess { tracks ->
                    allTracks = tracks
                    publishTrackState(isLoading = false, errorMessage = null)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Error loading local music"
                        )
                    }
                }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            observeUserPreferencesUseCase().collect { prefs ->
                currentPreferences = prefs
                publishTrackState()
            }
        }
    }

    private fun publishTrackState(
        isLoading: Boolean = _uiState.value.isLoading,
        errorMessage: String? = _uiState.value.errorMessage
    ) {
        _uiState.update { state ->
            val visibleTracks = applyLibrarySourceFilter(allTracks, currentPreferences)
            state.copy(
                tracks = visibleTracks,
                filteredTracks = filterTracks(visibleTracks, state.searchQuery),
                favoriteTracks = visibleTracks.filterFavorites(currentPreferences),
                recentTracks = visibleTracks.filterRecents(currentPreferences),
                activeLibraryFolderLabel = currentPreferences.manualLibraryFolderLabel,
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }
    }

    private fun filterTracks(tracks: List<Track>, query: String): List<Track> {
        if (query.isBlank()) return tracks
        return tracks.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true)
        }
    }

    private fun List<Track>.filterFavorites(preferences: UserPreferences): List<Track> {
        return filter { preferences.favoriteTrackIds.contains(it.id) }
            .sortedBy { it.title.lowercase() }
            .take(12)
    }

    private fun List<Track>.filterRecents(preferences: UserPreferences): List<Track> {
        if (preferences.recentTrackIds.isEmpty()) return emptyList()
        val tracksById = associateBy { it.id }
        return preferences.recentTrackIds.mapNotNull { tracksById[it] }.take(12)
    }

    private fun applyLibrarySourceFilter(
        tracks: List<Track>,
        preferences: UserPreferences
    ): List<Track> {
        val normalizedFolderToken = preferences.manualLibraryFolderUri
            ?.toFolderMatchToken()
            ?.takeIf { it.isNotBlank() }
            ?: return tracks

        return tracks.filter { track ->
            val normalizedSource = track.sourcePath
                ?.replace('\\', '/')
                ?.lowercase()
                .orEmpty()
            normalizedFolderToken in normalizedSource
        }
    }

    private fun String.toFolderMatchToken(): String? {
        return runCatching { Uri.parse(this) }
            .getOrNull()
            ?.lastPathSegment
            ?.substringAfter(':', "")
            ?.replace('\\', '/')
            ?.trim('/')
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }
    }
}
