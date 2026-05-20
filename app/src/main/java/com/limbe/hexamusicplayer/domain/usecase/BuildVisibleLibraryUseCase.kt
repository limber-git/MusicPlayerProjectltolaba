package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.model.UserPreferences
import com.limbe.hexamusicplayer.domain.model.VisibleLibrary
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class BuildVisibleLibraryUseCase {

    operator fun invoke(
        allTracks: List<Track>,
        preferences: UserPreferences,
        searchQuery: String
    ): VisibleLibrary {
        val visibleTracks = applyLibrarySourceFilter(allTracks, preferences)
        return VisibleLibrary(
            visibleTracks = visibleTracks,
            filteredTracks = filterTracks(visibleTracks, searchQuery),
            favoriteTracks = visibleTracks.filterFavorites(preferences),
            recentTracks = visibleTracks.filterRecents(preferences),
            activeFolderLabel = preferences.manualLibraryFolderLabel
        )
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
        return substringAfterLast('/', "")
            .let { encodedSegment ->
                runCatching {
                    URLDecoder.decode(encodedSegment, StandardCharsets.UTF_8.name())
                }.getOrDefault(encodedSegment)
            }
            .substringAfter(':', "")
            .replace('\\', '/')
            .trim('/')
            .lowercase()
            .takeIf { it.isNotBlank() }
    }
}
