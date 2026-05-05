package com.limbe.hexamusicplayer

import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.port.LocalMusicRepository
import com.limbe.hexamusicplayer.domain.usecase.GetLocalTracksUseCase
import com.limbe.hexamusicplayer.ui.screens.library.LibraryViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `refresh tracks loads local songs on success`() = runTest {
        val fakeRepository = FakeLocalMusicRepository(
            tracks = listOf(
                Track(
                    id = 1L,
                    title = "Track One",
                    artist = "Artist A",
                    durationMs = 120_000L,
                    contentUri = "content://song/1"
                )
            )
        )

        val viewModel = LibraryViewModel(GetLocalTracksUseCase(fakeRepository))

        viewModel.refreshTracks()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.tracks.size)
        assertEquals("Track One", viewModel.uiState.value.tracks.first().title)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `refresh tracks exposes error on failure`() = runTest {
        val fakeRepository = FakeLocalMusicRepository(
            error = IllegalStateException("Storage unavailable")
        )

        val viewModel = LibraryViewModel(GetLocalTracksUseCase(fakeRepository))

        viewModel.refreshTracks()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.tracks.isEmpty())
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals("Storage unavailable", viewModel.uiState.value.errorMessage)
    }
}

private class FakeLocalMusicRepository(
    private val tracks: List<Track> = emptyList(),
    private val error: Throwable? = null
) : LocalMusicRepository {
    override suspend fun listLocalTracks(): List<Track> {
        error?.let { throw it }
        return tracks
    }
}
