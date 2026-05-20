package com.limbe.hexamusicplayer.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.limbe.hexamusicplayer.R
import com.limbe.hexamusicplayer.domain.model.RepeatMode

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val durationMs = uiState.durationMs.coerceAtLeast(0L)
    val playbackProgress = if (durationMs > 0L) {
        (uiState.currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    var sliderProgress by remember(uiState.currentTrack?.id) {
        mutableFloatStateOf(playbackProgress)
    }
    var isScrubbing by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(playbackProgress, isScrubbing, uiState.currentTrack?.id) {
        if (!isScrubbing) {
            sliderProgress = playbackProgress
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AtmosphericBackdrop(uiState = uiState)

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.action_close),
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(32.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = uiState.currentTrack?.artworkUri ?: uiState.currentTrack?.contentUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = androidx.compose.ui.graphics.painter.ColorPainter(MaterialTheme.colorScheme.primary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.currentTrack?.title ?: stringResource(R.string.player_idle_title),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = uiState.currentTrack?.artist ?: stringResource(R.string.player_idle_subtitle),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        uiState.currentTrack?.album?.takeIf { it.isNotBlank() }?.let { album ->
                            Text(
                                text = album,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.toggleFavorite()
                        }
                    ) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.action_favorite),
                            tint = if (uiState.isFavorite) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onBackground
                            },
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                uiState.playerErrorMessage?.let { errorMessage ->
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Slider(
                        value = sliderProgress,
                        onValueChange = { ratio ->
                            isScrubbing = true
                            sliderProgress = ratio
                        },
                        onValueChangeFinished = {
                            if (durationMs > 0L) {
                                viewModel.seekTo((durationMs * sliderProgress).toLong())
                            }
                            isScrubbing = false
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.onBackground,
                            activeTrackColor = MaterialTheme.colorScheme.onBackground,
                            inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(uiState.currentPositionMs),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Text(
                            text = formatDuration(durationMs),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.toggleShuffle()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = stringResource(R.string.action_shuffle),
                            tint = if (uiState.shuffleModeEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            }
                        )
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.skipToPrevious()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = stringResource(R.string.action_previous),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.togglePlayback()
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = stringResource(R.string.action_play_pause),
                                tint = MaterialTheme.colorScheme.background,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.skipToNext()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = stringResource(R.string.action_next),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.toggleRepeat()
                        }
                    ) {
                        val repeatIcon = when (uiState.repeatMode) {
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        }
                        Icon(
                            imageVector = repeatIcon,
                            contentDescription = stringResource(R.string.action_repeat),
                            tint = if (uiState.repeatMode != RepeatMode.OFF) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun AtmosphericBackdrop(uiState: PlayerUiState) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = uiState.currentTrack?.artworkUri ?: uiState.currentTrack?.contentUri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(60.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}
