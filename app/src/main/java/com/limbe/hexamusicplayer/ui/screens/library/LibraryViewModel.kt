package com.limbe.hexamusicplayer.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limbe.hexamusicplayer.domain.usecase.GetLocalTracksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val getLocalTracksUseCase: GetLocalTracksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        refreshTracks()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredTracks = if (query.isBlank()) {
                    state.tracks
                } else {
                    state.tracks.filter {
                        it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true)
                    }
                }
            )
        }
    }

    fun refreshTracks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { getLocalTracksUseCase() }
                .onSuccess { tracks ->
                    _uiState.update {
                        it.copy(
                            tracks = tracks,
                            filteredTracks = if (it.searchQuery.isBlank()) tracks else {
                                tracks.filter { t ->
                                    t.title.contains(it.searchQuery, ignoreCase = true) ||
                                    t.artist.contains(it.searchQuery, ignoreCase = true)
                                }
                            },
                            isLoading = false,
                            errorMessage = null
                        )
                    }
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
}
