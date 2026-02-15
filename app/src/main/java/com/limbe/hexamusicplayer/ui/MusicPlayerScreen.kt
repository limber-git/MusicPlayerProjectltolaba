package com.limbe.hexamusicplayer.ui

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.infrastructure.service.PlaybackMediaSessionService
import com.limbe.hexamusicplayer.ui.components.LabeledSlider
import com.limbe.hexamusicplayer.ui.components.TrackRow
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MusicPlayerScreen(
    viewModel: MusicPlayerViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permission = remember { requiredAudioPermission() }
    var hasPermission by remember { mutableStateOf(hasAudioPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            viewModel.refreshTracks()
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission && uiState.tracks.isEmpty() && !uiState.isLoading) {
            viewModel.refreshTracks()
        }
    }

    LaunchedEffect(uiState.currentTrack?.id, uiState.isPlaying) {
        if (uiState.currentTrack != null || uiState.isPlaying) {
            PlaybackMediaSessionService.start(context.applicationContext)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Column {
                        Text(
                            text = "HEXA SOUND",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Native Android Studio Player",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refreshTracks) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar biblioteca",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF091124),
                            Color(0xFF0D1D3B),
                            Color(0xFF121A2F)
                        )
                    )
                )
        ) {
            AtmosphericBackdrop()

            if (!hasPermission) {
                PermissionContent(
                    paddingValues = paddingValues,
                    onRequestPermission = { permissionLauncher.launch(permission) }
                )
            } else {
                MusicPlayerContent(
                    uiState = uiState,
                    paddingValues = paddingValues,
                    onPlayPause = viewModel::togglePlayback,
                    onSetSpeed = viewModel::setSpeed,
                    onSetPitch = viewModel::setPitch,
                    onSeekTo = viewModel::seekTo,
                    onBandLevelChange = viewModel::setEqBandLevel,
                    onBassStrengthChange = viewModel::setBassStrength,
                    onVirtualizerStrengthChange = viewModel::setVirtualizerStrength,
                    onLoudnessGainChange = viewModel::setLoudnessGain,
                    onTrackClick = viewModel::playTrack
                )
            }
        }
    }
}

@Composable
private fun AtmosphericBackdrop() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 110.dp)
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .graphicsLayer(alpha = 0.24f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF5CF2D7), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomStart)
                .graphicsLayer(alpha = 0.16f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFFF9B6A), Color.Transparent)
                    )
                )
        )
    }
}

@Composable
private fun PermissionContent(
    paddingValues: PaddingValues,
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xCC15233F))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Audiotrack,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Permite acceso a tu biblioteca local",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Necesitamos permiso para cargar canciones y habilitar el estudio de audio.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onRequestPermission) {
                    Text("Conceder permiso")
                }
            }
        }
    }
}

@Composable
private fun MusicPlayerContent(
    uiState: MusicPlayerUiState,
    paddingValues: PaddingValues,
    onPlayPause: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSetPitch: (Float) -> Unit,
    onSeekTo: (Long) -> Unit,
    onBandLevelChange: (Int, Int) -> Unit,
    onBassStrengthChange: (Int) -> Unit,
    onVirtualizerStrengthChange: (Int) -> Unit,
    onLoudnessGainChange: (Int) -> Unit,
    onTrackClick: (Track) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        item {
            HeroNowPlayingCard(
                uiState = uiState,
                onPlayPause = onPlayPause
            )
        }

        item {
            TransportStudioCard(
                uiState = uiState,
                onSeekTo = onSeekTo,
                onSetSpeed = onSetSpeed,
                onSetPitch = onSetPitch
            )
        }

        item {
            StudioEqualizerCard(
                uiState = uiState,
                onBandLevelChange = onBandLevelChange,
                onBassStrengthChange = onBassStrengthChange,
                onVirtualizerStrengthChange = onVirtualizerStrengthChange,
                onLoudnessGainChange = onLoudnessGainChange
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Biblioteca Local",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${uiState.tracks.size} canciones",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        if (uiState.tracks.isEmpty() && !uiState.isLoading) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xAA1C2C4F)
                ) {
                    Text(
                        text = "No se encontraron canciones en el dispositivo.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        items(items = uiState.tracks, key = { it.id }) { track ->
            TrackRow(
                track = track,
                isCurrent = uiState.currentTrack?.id == track.id,
                isPlaying = uiState.isPlaying,
                onClick = { onTrackClick(track) }
            )
        }

        item {
            Box(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeroNowPlayingCard(
    uiState: MusicPlayerUiState,
    onPlayPause: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "disc-rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing)
        ),
        label = "disc-angle"
    )

    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC18284A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(94.dp)
                    .graphicsLayer {
                        rotationZ = if (uiState.isPlaying) rotation else 0f
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(
                                Color(0xFF74F9D9),
                                Color(0xFFFFC16E),
                                Color(0xFFFF7D70),
                                Color(0xFF74F9D9)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0B1428))
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = uiState.currentTrack?.title ?: "Selecciona una cancion",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = uiState.currentTrack?.artist ?: "Sin artista",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (uiState.isPlaying) "Reproduciendo en segundo plano" else "Pausado",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            FilledIconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(58.dp)
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
private fun TransportStudioCard(
    uiState: MusicPlayerUiState,
    onSeekTo: (Long) -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSetPitch: (Float) -> Unit
) {
    val durationMs = uiState.durationMs.coerceAtLeast(0L)
    val progress = if (durationMs > 0) {
        (uiState.currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xC425365E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Control de Reproduccion",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Text(
                text = "${formatDuration(uiState.currentPositionMs)} / ${formatDuration(durationMs)}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Slider(
                value = progress,
                onValueChange = { ratio ->
                    if (durationMs > 0) {
                        onSeekTo((durationMs * ratio).toLong())
                    }
                },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            LabeledSlider(
                label = "Velocidad",
                value = uiState.speed,
                valueRange = 0.5f..2.0f,
                valueText = String.format(Locale.US, "%.2fx", uiState.speed),
                onValueChange = onSetSpeed
            )

            PresetChips(
                values = listOf(0.75f, 1f, 1.25f, 1.5f),
                currentValue = uiState.speed,
                label = "Presets",
                onPick = onSetSpeed
            )

            LabeledSlider(
                label = "Tonalidad",
                value = uiState.pitch,
                valueRange = 0.5f..2.0f,
                valueText = String.format(Locale.US, "%.2fx", uiState.pitch),
                onValueChange = onSetPitch
            )

            PresetChips(
                values = listOf(0.9f, 1f, 1.1f, 1.25f),
                currentValue = uiState.pitch,
                label = "Tonalidad rápida",
                onPick = onSetPitch
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetChips(
    values: List<Float>,
    currentValue: Float,
    label: String,
    onPick: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            values.forEach { value ->
                val selected = kotlin.math.abs(currentValue - value) < 0.02f
                SuggestionChip(
                    onClick = { onPick(value) },
                    label = { Text(String.format(Locale.US, "%.2fx", value)) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (selected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        },
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@Composable
private fun StudioEqualizerCard(
    uiState: MusicPlayerUiState,
    onBandLevelChange: (Int, Int) -> Unit,
    onBassStrengthChange: (Int) -> Unit,
    onVirtualizerStrengthChange: (Int) -> Unit,
    onLoudnessGainChange: (Int) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xC22A1F45))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "Studio EQ",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
                FilledTonalIconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.attachedSessionId == null || uiState.eqBands.isEmpty()) {
                        Text(
                            text = "Reproduce una cancion para habilitar el ecualizador avanzado.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        uiState.eqBands.forEach { band ->
                            val freqKhz = band.centerFreqHz / 1000f
                            LabeledSlider(
                                label = String.format(Locale.US, "Banda %.1f kHz", freqKhz),
                                value = band.level.toFloat(),
                                valueRange = band.minLevel.toFloat()..band.maxLevel.toFloat(),
                                valueText = "${band.level} mB",
                                onValueChange = { onBandLevelChange(band.index, it.toInt()) }
                            )
                        }

                        LabeledSlider(
                            label = "Bass Boost",
                            value = uiState.bassStrength.toFloat(),
                            valueRange = 0f..1000f,
                            valueText = "${uiState.bassStrength}",
                            onValueChange = { onBassStrengthChange(it.toInt()) }
                        )

                        LabeledSlider(
                            label = "Virtualizer",
                            value = uiState.virtualizerStrength.toFloat(),
                            valueRange = 0f..1000f,
                            valueText = "${uiState.virtualizerStrength}",
                            onValueChange = { onVirtualizerStrengthChange(it.toInt()) }
                        )

                        LabeledSlider(
                            label = "Loudness",
                            value = uiState.loudnessGainMb.toFloat(),
                            valueRange = -1500f..3000f,
                            valueText = "${uiState.loudnessGainMb} mB",
                            onValueChange = { onLoudnessGainChange(it.toInt()) }
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}

private fun requiredAudioPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

private fun hasAudioPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        requiredAudioPermission()
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}
