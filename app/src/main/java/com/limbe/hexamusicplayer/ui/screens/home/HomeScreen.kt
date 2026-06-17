package com.limbe.hexamusicplayer.ui.screens.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.limbe.hexamusicplayer.R
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.ui.components.TrackMenuAction
import com.limbe.hexamusicplayer.ui.components.TrackRow
import com.limbe.hexamusicplayer.ui.components.rememberArtworkImageRequest
import com.limbe.hexamusicplayer.ui.screens.library.LibraryViewModel
import com.limbe.hexamusicplayer.ui.screens.player.PlayerUiState
import com.limbe.hexamusicplayer.ui.util.hasAudioPermission
import com.limbe.hexamusicplayer.ui.util.requiredAudioPermission

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    libraryViewModel: LibraryViewModel,
    playerUiState: PlayerUiState,
    onTrackClick: (Track, List<Track>) -> Unit,
    onTrackPlayNext: (Track) -> Unit,
    onTrackAddToQueue: (Track) -> Unit,
    onOpenPlayer: () -> Unit,
    onOpenStudio: () -> Unit,
    onOpenExplorer: () -> Unit,
    onOpenSettings: () -> Unit
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.action_open_settings)
                        )
                    }
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

        val spotlightAlbums = remember(uiState.tracks) { uiState.tracks.groupBy { it.albumId ?: it.album }.values.map { it.first() }.take(8) }
        val spotlightArtists = remember(uiState.tracks) { uiState.tracks.groupBy { it.artist }.values.map { it.first() }.take(6) }

        val playNextLabel = stringResource(R.string.action_play_next)
        val addToQueueLabel = stringResource(R.string.action_add_to_queue)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item(contentType = "hero") {
                HeroCard(
                    playerUiState = playerUiState,
                    favoriteCount = uiState.favoriteTracks.size,
                    onOpenPlayer = onOpenPlayer,
                    onOpenStudio = onOpenStudio
                )
            }
            item(contentType = "shortcuts") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShortcutCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.home_shortcut_explore_title),
                        body = stringResource(R.string.home_shortcut_explore_body),
                        icon = Icons.Default.FolderOpen,
                        onClick = onOpenExplorer
                    )
                    ShortcutCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.home_shortcut_studio_title),
                        body = stringResource(R.string.home_shortcut_studio_body),
                        icon = Icons.Default.AutoAwesome,
                        onClick = onOpenStudio
                    )
                }
            }
            if (uiState.recentTracks.isNotEmpty()) {
                item(contentType = "section_title") {
                    SectionTitle(title = stringResource(R.string.home_recent_title))
                }
                items(
                    items = uiState.recentTracks.take(4),
                    key = { "home-recent-${it.id}" },
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
                        onClick = { onTrackClick(track, uiState.recentTracks) },
                        menuActions = menuActions,
                        modifier = Modifier.animateItem()
                    )
                }
            }
            if (uiState.favoriteTracks.isNotEmpty()) {
                item(contentType = "section_title") {
                    SectionTitle(title = stringResource(R.string.home_favorites_title))
                }
                items(
                    items = uiState.favoriteTracks.take(4),
                    key = { "home-fav-${it.id}" },
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
                        onClick = { onTrackClick(track, uiState.favoriteTracks) },
                        menuActions = menuActions,
                        modifier = Modifier.animateItem()
                    )
                }
            }
            if (spotlightAlbums.isNotEmpty()) {
                item(contentType = "section_title") {
                    SectionTitle(title = stringResource(R.string.home_albums_title))
                }
                item(contentType = "album_row") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(
                            items = spotlightAlbums,
                            key = { "album-${it.id}" },
                            contentType = { "album_card" }
                        ) { track ->
                            MediaSpotlightCard(
                                title = track.album,
                                subtitle = track.artist,
                                artworkUri = track.artworkUri ?: track.contentUri,
                                onClick = {
                                    val queue = uiState.tracks.filter { candidate ->
                                        candidate.albumId == track.albumId || candidate.album == track.album
                                    }
                                    onTrackClick(track, queue.ifEmpty { listOf(track) })
                                }
                            )
                        }
                    }
                }
            }
            if (spotlightArtists.isNotEmpty()) {
                item(contentType = "section_title") {
                    SectionTitle(title = stringResource(R.string.home_artists_title))
                }
                item(contentType = "artist_row") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(
                            items = spotlightArtists,
                            key = { "artist-${it.artist}" },
                            contentType = { "artist_card" }
                        ) { track ->
                            MediaSpotlightCard(
                                title = track.artist,
                                subtitle = track.album,
                                artworkUri = track.artworkUri ?: track.contentUri,
                                onClick = {
                                    val queue = uiState.tracks.filter { candidate -> candidate.artist == track.artist }
                                    onTrackClick(track, queue.ifEmpty { listOf(track) })
                                }
                            )
                        }
                    }
                }
            }
            item(contentType = "spacer") {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun HeroCard(
    playerUiState: PlayerUiState,
    favoriteCount: Int,
    onOpenPlayer: () -> Unit,
    onOpenStudio: () -> Unit
) {
    val currentTrack = playerUiState.currentTrack

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (currentTrack != null) stringResource(R.string.home_hero_active_label) else stringResource(R.string.home_hero_idle_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Text(
                    text = currentTrack?.title ?: stringResource(R.string.home_hero_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = currentTrack?.artist ?: pluralStringResource(
                        R.plurals.home_hero_body,
                        favoriteCount,
                        favoriteCount
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        onClick = onOpenPlayer,
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text(
                            text = stringResource(R.string.home_hero_cta_player),
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        onClick = onOpenStudio,
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = stringResource(R.string.home_hero_cta_studio),
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortcutCard(
    title: String,
    body: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MediaSpotlightCard(
    title: String,
    subtitle: String,
    artworkUri: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(148.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .clip(RoundedCornerShape(18.dp))
        ) {
            AsyncImage(
                model = rememberArtworkImageRequest(
                    data = artworkUri,
                    width = 148.dp,
                    height = 148.dp,
                    cacheKey = "spotlight-$artworkUri"
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                error = androidx.compose.ui.graphics.painter.ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun PermissionState(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LibraryMusic,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.permission_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.permission_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            onClick = onRequestPermission,
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = stringResource(R.string.permission_cta),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
