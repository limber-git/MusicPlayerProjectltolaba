package com.limbe.hexamusicplayer.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.model.UserPreferences
import com.limbe.hexamusicplayer.domain.usecase.BuildVisibleLibraryUseCase
import com.limbe.hexamusicplayer.domain.usecase.GetLocalTracksUseCase
import com.limbe.hexamusicplayer.domain.usecase.ObserveUserPreferencesUseCase
import kotlinx.coroutines.CoroutineDispatcher
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
    private val buildVisibleLibraryUseCase: BuildVisibleLibraryUseCase,
    private val libraryDispatcher: CoroutineDispatcher = Dispatchers.Default
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
                    publishTrackState(
                        isLoading = false,
                        hasLoadedOnce = true,
                        errorMessage = null,
                        replaceErrorMessage = true
                    )
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
        isLoading: Boolean? = null,
        hasLoadedOnce: Boolean? = null,
        debounceMs: Long = 0L,
        errorMessage: String? = null,
        replaceErrorMessage: Boolean = false
    ) {
        publishJob?.cancel()
        publishJob = viewModelScope.launch {
            if (debounceMs > 0L) {
                delay(debounceMs)
            }
            val state = _uiState.value
            val visibleLibrary = withContext(libraryDispatcher) {
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
                    isLoading = isLoading ?: it.isLoading,
                    hasLoadedOnce = hasLoadedOnce ?: it.hasLoadedOnce,
                    errorMessage = if (replaceErrorMessage) errorMessage else it.errorMessage
                )
            }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 250L
    }
}
