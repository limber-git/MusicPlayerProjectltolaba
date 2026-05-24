package com.limbe.hexamusicplayer.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.model.UserPreferences
import com.limbe.hexamusicplayer.domain.usecase.BuildVisibleLibraryUseCase
import com.limbe.hexamusicplayer.domain.usecase.GetLocalTracksUseCase
import com.limbe.hexamusicplayer.domain.usecase.ObserveUserPreferencesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryViewModel(
    private val getLocalTracksUseCase: GetLocalTracksUseCase,
    private val observeUserPreferencesUseCase: ObserveUserPreferencesUseCase,
    private val buildVisibleLibraryUseCase: BuildVisibleLibraryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private var currentPreferences = UserPreferences()
    private var allTracks: List<Track> = emptyList()
    private var publishJob: Job? = null

    init {
        observePreferences()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        publishTrackState(debounceMs = SEARCH_DEBOUNCE_MS)
    }

    fun refreshTracks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { getLocalTracksUseCase() }
                .onSuccess { tracks ->
                    allTracks = tracks
                    publishTrackState(isLoading = false, hasLoadedOnce = true, errorMessage = null)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasLoadedOnce = true,
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
        hasLoadedOnce: Boolean = _uiState.value.hasLoadedOnce,
        debounceMs: Long = 0L,
        errorMessage: String? = _uiState.value.errorMessage
    ) {
        publishJob?.cancel()
        publishJob = viewModelScope.launch {
            if (debounceMs > 0L) {
                delay(debounceMs)
            }
            val state = _uiState.value
            val visibleLibrary = withContext(Dispatchers.Default) {
                buildVisibleLibraryUseCase(
                    allTracks = allTracks,
                    preferences = currentPreferences,
                    searchQuery = state.searchQuery
                )
            }
            _uiState.update {
                it.copy(
                    tracks = visibleLibrary.visibleTracks,
                    filteredTracks = visibleLibrary.filteredTracks,
                    favoriteTracks = visibleLibrary.favoriteTracks,
                    recentTracks = visibleLibrary.recentTracks,
                    activeLibraryFolderLabel = visibleLibrary.activeFolderLabel,
                    isLoading = isLoading,
                    hasLoadedOnce = hasLoadedOnce,
                    errorMessage = errorMessage
                )
            }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 250L
    }
}
