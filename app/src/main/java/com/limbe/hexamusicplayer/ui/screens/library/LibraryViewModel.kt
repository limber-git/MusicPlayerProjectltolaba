package com.limbe.hexamusicplayer.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.model.UserPreferences
import com.limbe.hexamusicplayer.domain.usecase.BuildVisibleLibraryUseCase
import com.limbe.hexamusicplayer.domain.usecase.GetLocalTracksUseCase
import com.limbe.hexamusicplayer.domain.usecase.ObserveUserPreferencesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val getLocalTracksUseCase: GetLocalTracksUseCase,
    private val observeUserPreferencesUseCase: ObserveUserPreferencesUseCase,
    private val buildVisibleLibraryUseCase: BuildVisibleLibraryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private var currentPreferences = UserPreferences()
    private var allTracks: List<Track> = emptyList()

    init {
        observePreferences()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        publishTrackState()
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
            val visibleLibrary = buildVisibleLibraryUseCase(
                allTracks = allTracks,
                preferences = currentPreferences,
                searchQuery = state.searchQuery
            )
            state.copy(
                tracks = visibleLibrary.visibleTracks,
                filteredTracks = visibleLibrary.filteredTracks,
                favoriteTracks = visibleLibrary.favoriteTracks,
                recentTracks = visibleLibrary.recentTracks,
                activeLibraryFolderLabel = visibleLibrary.activeFolderLabel,
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }
    }
}
