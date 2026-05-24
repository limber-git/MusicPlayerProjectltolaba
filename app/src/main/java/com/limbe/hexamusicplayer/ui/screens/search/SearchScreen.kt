package com.limbe.hexamusicplayer.ui.screens.search

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.limbe.hexamusicplayer.R
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.ui.components.TrackMenuAction
import com.limbe.hexamusicplayer.ui.components.TrackRow
import com.limbe.hexamusicplayer.ui.screens.home.PermissionState
import com.limbe.hexamusicplayer.ui.screens.library.LibraryViewModel
import com.limbe.hexamusicplayer.ui.screens.player.PlayerUiState
import com.limbe.hexamusicplayer.ui.util.hasAudioPermission
import com.limbe.hexamusicplayer.ui.util.requiredAudioPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    libraryViewModel: LibraryViewModel,
    playerUiState: PlayerUiState,
    onTrackClick: (Track, List<Track>) -> Unit,
    onTrackPlayNext: (Track) -> Unit,
    onTrackAddToQueue: (Track) -> Unit
) {
    val uiState by libraryViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasAudioPermission(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            libraryViewModel.refreshTracks()
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission && !uiState.hasLoadedOnce && !uiState.isLoading) {
            libraryViewModel.refreshTracks()
        }
    }

    val albums = remember(uiState.filteredTracks) { uiState.filteredTracks.map { it.album }.distinct().take(5) }
    val artists = remember(uiState.filteredTracks) { uiState.filteredTracks.map { it.artist }.distinct().take(5) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                title = {
                    Text(
                        text = stringResource(R.string.search_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
        }
    ) { paddingValues ->
        if (!hasPermission) {
            PermissionState(
                modifier = Modifier.padding(paddingValues),
                onRequestPermission = { permissionLauncher.launch(requiredAudioPermission()) }
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = libraryViewModel::onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search_title)
                        )
                    },
                    singleLine = true
                )
            }

            if (uiState.searchQuery.isBlank()) {
                item {
                    EmptySearchState(text = stringResource(R.string.search_empty_prompt))
                }
            } else {
                if (artists.isNotEmpty()) {
                    item {
                        ResultSummaryCard(
                            title = stringResource(R.string.search_artists_title),
                            values = artists
                        )
                    }
                }
                if (albums.isNotEmpty()) {
                    item {
                        ResultSummaryCard(
                            title = stringResource(R.string.search_albums_title),
                            values = albums
                        )
                    }
                }
                if (uiState.filteredTracks.isEmpty()) {
                    item {
                        EmptySearchState(text = stringResource(R.string.search_no_results, uiState.searchQuery))
                    }
                } else {
                    item {
                        Text(
                            text = stringResource(R.string.search_tracks_title, uiState.filteredTracks.size),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    items(uiState.filteredTracks, key = { "search-${it.id}" }) { track ->
                        TrackRow(
                            track = track,
                            isCurrent = playerUiState.currentTrack?.id == track.id,
                            isPlaying = playerUiState.isPlaying,
                            onClick = { onTrackClick(track, uiState.filteredTracks) },
                            menuActions = searchTrackMenuActions(track, onTrackPlayNext, onTrackAddToQueue)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun searchTrackMenuActions(
    track: Track,
    onTrackPlayNext: (Track) -> Unit,
    onTrackAddToQueue: (Track) -> Unit
): List<TrackMenuAction> {
    return listOf(
        TrackMenuAction(
            label = stringResource(R.string.action_play_next),
            onClick = { onTrackPlayNext(track) }
        ),
        TrackMenuAction(
            label = stringResource(R.string.action_add_to_queue),
            onClick = { onTrackAddToQueue(track) }
        )
    )
}

@Composable
private fun ResultSummaryCard(
    title: String,
    values: List<String>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = values.joinToString(" - "),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptySearchState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.ManageSearch,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
