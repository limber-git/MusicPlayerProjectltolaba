package com.limbe.hexamusicplayer.ui.screens.explorer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.limbe.hexamusicplayer.R
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.ui.components.rememberArtworkImageRequest
import com.limbe.hexamusicplayer.ui.screens.library.LibraryViewModel
import com.limbe.hexamusicplayer.ui.util.hasAudioPermission
import com.limbe.hexamusicplayer.ui.util.requiredAudioPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerScreen(
    viewModel: LibraryViewModel,
    onAlbumClick: (Track, List<Track>) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasAudioPermission(context)) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            viewModel.refreshTracks()
        }
    }
    val albums = remember(uiState.tracks) { buildAlbums(uiState.tracks) }
    val artists = remember(uiState.tracks) { buildArtists(uiState.tracks) }

    LaunchedEffect(hasPermission) {
        if (hasPermission && !uiState.hasLoadedOnce && !uiState.isLoading) {
            viewModel.refreshTracks()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                    title = {
                        Text(
                            text = stringResource(R.string.explorer_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(stringResource(R.string.explorer_tab_albums), fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(stringResource(R.string.explorer_tab_artists), fontSize = 12.sp) }
                    )
                }
            }
        }
    ) { paddingValues ->
        when {
            !hasPermission -> PermissionContent(
                modifier = Modifier.padding(paddingValues),
                onRequestPermission = { permissionLauncher.launch(requiredAudioPermission()) }
            )

            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            selectedTab == 0 && albums.isEmpty() -> EmptyCollectionContent(
                modifier = Modifier.padding(paddingValues),
                title = stringResource(R.string.explorer_empty_title),
                body = stringResource(R.string.explorer_empty_body),
                icon = Icons.Default.Album
            )

            selectedTab == 1 && artists.isEmpty() -> EmptyCollectionContent(
                modifier = Modifier.padding(paddingValues),
                title = stringResource(R.string.explorer_empty_artists_title),
                body = stringResource(R.string.explorer_empty_artists_body),
                icon = Icons.Default.Person
            )

            selectedTab == 0 -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(items = albums, key = { it.key }) { album ->
                    AlbumCard(
                        album = album,
                        onClick = { onAlbumClick(album.tracks.first(), album.tracks) }
                    )
                }
            }

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(items = artists, key = { it.key }) { artist ->
                    ArtistCard(
                        artist = artist,
                        onClick = { onAlbumClick(artist.tracks.first(), artist.tracks) }
                    )
                }
            }
        }
    }
}

private data class AlbumUiModel(
    val key: String,
    val title: String,
    val artist: String,
    val artworkUri: String,
    val tracks: List<Track>
)

private data class ArtistUiModel(
    val key: String,
    val name: String,
    val artworkUri: String,
    val albumCount: Int,
    val tracks: List<Track>
)

private fun buildAlbums(tracks: List<Track>): List<AlbumUiModel> {
    return tracks
        .groupBy { track -> track.albumId ?: track.album }
        .values
        .map { albumTracks ->
            val firstTrack = albumTracks.first()
            AlbumUiModel(
                key = "${firstTrack.albumId ?: firstTrack.album}-${firstTrack.artist}",
                title = firstTrack.album,
                artist = firstTrack.artist,
                artworkUri = firstTrack.artworkUri ?: firstTrack.contentUri,
                tracks = albumTracks
            )
        }
        .sortedBy { it.title.lowercase() }
}

private fun buildArtists(tracks: List<Track>): List<ArtistUiModel> {
    return tracks
        .groupBy { it.artist }
        .map { (artistName, artistTracks) ->
            ArtistUiModel(
                key = artistName,
                name = artistName,
                artworkUri = artistTracks.firstOrNull()?.artworkUri ?: artistTracks.first().contentUri,
                albumCount = artistTracks.map { it.album }.distinct().size,
                tracks = artistTracks.sortedBy { it.album.lowercase() }
            )
        }
        .sortedBy { it.name.lowercase() }
}

@Composable
private fun AlbumCard(
    album: AlbumUiModel,
    onClick: () -> Unit
) {
    CollectionCard(
        title = album.title,
        subtitle = album.artist,
        metadata = stringResource(R.string.explorer_track_count, album.tracks.size),
        artworkUri = album.artworkUri,
        onClick = onClick
    )
}

@Composable
private fun ArtistCard(
    artist: ArtistUiModel,
    onClick: () -> Unit
) {
    CollectionCard(
        title = artist.name,
        subtitle = stringResource(R.string.explorer_artist_album_count, artist.albumCount),
        metadata = stringResource(R.string.explorer_track_count, artist.tracks.size),
        artworkUri = artist.artworkUri,
        onClick = onClick
    )
}

@Composable
private fun CollectionCard(
    title: String,
    subtitle: String,
    metadata: String,
    artworkUri: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = rememberArtworkImageRequest(
                data = artworkUri,
                width = 180.dp,
                height = 180.dp,
                cacheKey = "collection-$artworkUri"
            ),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            error = androidx.compose.ui.graphics.painter.ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = metadata,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyCollectionContent(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PermissionContent(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Audiotrack,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.permission_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.permission_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRequestPermission) {
            Text(stringResource(R.string.permission_cta))
        }
    }
}
