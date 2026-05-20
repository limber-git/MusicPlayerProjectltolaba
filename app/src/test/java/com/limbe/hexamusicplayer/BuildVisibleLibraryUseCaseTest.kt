package com.limbe.hexamusicplayer

import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.model.UserPreferences
import com.limbe.hexamusicplayer.domain.usecase.BuildVisibleLibraryUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildVisibleLibraryUseCaseTest {

    private val useCase = BuildVisibleLibraryUseCase()

    @Test
    fun `builds filtered favorites and recents in one pass`() {
        val tracks = listOf(
            Track(
                id = 1L,
                title = "Warm Up",
                artist = "Band A",
                album = "Practice",
                albumId = 4L,
                durationMs = 120_000L,
                contentUri = "content://song/1",
                sourcePath = "Music/Practice/"
            ),
            Track(
                id = 2L,
                title = "Live Cut",
                artist = "Band B",
                album = "Tour",
                albumId = 5L,
                durationMs = 150_000L,
                contentUri = "content://song/2",
                sourcePath = "Download/Shows/"
            )
        )

        val result = useCase(
            allTracks = tracks,
            preferences = UserPreferences(
                favoriteTrackIds = setOf(1L),
                recentTrackIds = listOf(2L, 1L),
                manualLibraryFolderUri = "content://com.android.externalstorage.documents/tree/primary%3AMusic%2FPractice",
                manualLibraryFolderLabel = "Practice"
            ),
            searchQuery = "Warm"
        )

        assertEquals(1, result.visibleTracks.size)
        assertEquals(1, result.filteredTracks.size)
        assertEquals(1, result.favoriteTracks.size)
        assertEquals(1, result.recentTracks.size)
        assertEquals("Practice", result.activeFolderLabel)
    }
}
