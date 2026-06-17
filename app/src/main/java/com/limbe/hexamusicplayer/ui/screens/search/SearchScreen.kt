package com.limbe.hexamusicplayer.ui.screens.search

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
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

    val playNextLabel = stringResource(R.string.action_play_next)
    val addToQueueLabel = stringResource(R.string.action_add_to_queue)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
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
            item(contentType = "search_field") {
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
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            }

            if (uiState.searchQuery.isBlank()) {
                item(contentType = "empty_state") {
                    EmptySearchState(text = stringResource(R.string.search_empty_prompt))
                }
            } else {
                if (artists.isNotEmpty()) {
                    item(contentType = "summary_card") {
                        ResultSummaryCard(
                            title = stringResource(R.string.search_artists_title),
                            values = artists
                        )
                    }
                }
                if (albums.isNotEmpty()) {
                    item(contentType = "summary_card") {
                        ResultSummaryCard(
                            title = stringResource(R.string.search_albums_title),
                            values = albums
                        )
                    }
                }
                if (uiState.filteredTracks.isEmpty()) {
                    item(contentType = "empty_state") {
                        EmptySearchState(text = stringResource(R.string.search_no_results, uiState.searchQuery))
                    }
                } else {
                    item(contentType = "section_title") {
                        androidx.compose.foundation.layout.Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = stringResource(R.string.search_tracks_title, uiState.filteredTracks.size),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    items(
                        items = uiState.filteredTracks,
                        key = { "search-${it.id}" },
                        contentType = { "track" }
                    ) { track ->
                        val menuActions = remember(track, onTrackPlayNext, onTrackAddToQueue) {
                            listOf(
                                TrackMenuAction(label = playNextLabel, onClick = { onTrackPlayNext(track) }),
                                TrackMenuAction(label = addToQueueLabel, onClick = { onTrackAddToQueue(track) })
                            )
                        }
                        TrackRow(
                            track = track,
                            isCurrent = playerUiState.currentTrack?.id == track.id,
                            isPlaying = playerUiState.isPlaying,
                            onClick = { onTrackClick(track, uiState.filteredTracks) },
                            menuActions = menuActions,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }

            item(contentType = "spacer") {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ResultSummaryCard(
    title: String,
    values: List<String>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            values.forEach { value ->
                androidx.compose.material3.Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = value,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
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
                imageVector = Icons.AutoMirrored.Filled.ManageSearch,
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
