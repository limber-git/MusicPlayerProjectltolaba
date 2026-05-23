package com.limbe.hexamusicplayer.ui.screens.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.limbe.hexamusicplayer.R
import com.limbe.hexamusicplayer.ui.components.LabeledSlider
import com.limbe.hexamusicplayer.ui.components.StudioFader
import com.limbe.hexamusicplayer.ui.screens.player.PlayerUiState
import com.limbe.hexamusicplayer.ui.screens.player.PlayerViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    viewModel: PlayerViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                        title = {
                            Text(
                                text = stringResource(R.string.studio_title),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    )

                    StudioHeroCard(uiState = uiState)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
                        tonalElevation = 10.dp,
                        shadowElevation = 0.dp
                    ) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            indicator = { tabPositions ->
                                if (selectedTab < tabPositions.size) {
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier
                                            .tabIndicatorOffset(tabPositions[selectedTab])
                                            .padding(horizontal = 16.dp),
                                        height = 3.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            divider = {}
                        ) {
                            StudioTab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = stringResource(R.string.studio_tab_engine),
                                icon = Icons.Default.Speed
                            )
                            StudioTab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = stringResource(R.string.studio_tab_equalizer),
                                icon = Icons.Default.GraphicEq
                            )
                            StudioTab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = stringResource(R.string.studio_tab_effects),
                                icon = Icons.Default.Waves
                            )
                            StudioTab(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                text = stringResource(R.string.studio_tab_lyrics),
                                icon = Icons.AutoMirrored.Filled.MenuBook
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    0 -> EngineTab(uiState, viewModel)
                    1 -> EqualizerTab(uiState, viewModel)
                    2 -> EffectsTab(uiState, viewModel)
                    3 -> LyricsTab(uiState)
                }
            }
        }
    }
}

@Composable
private fun StudioTab(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    icon: ImageVector
) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        },
        icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)) },
        selectedContentColor = MaterialTheme.colorScheme.primary,
        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun StudioHeroCard(uiState: PlayerUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        tonalElevation = 12.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StudioPill(text = "PRO SESSION")
                        Text(
                            text = uiState.currentTrack?.title ?: stringResource(R.string.studio_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.currentTrack?.artist ?: "Ajuste fino de speed, pitch y cadena de efectos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.studio_speed_label),
                        value = String.format(Locale.US, "%.2fx", uiState.speed)
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.studio_pitch_label),
                        value = String.format(Locale.US, "%.2fx", uiState.pitch)
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        label = "FX",
                        value = if (uiState.audioEffectsEnabled) "ON" else "SAFE"
                    )
                }
            }
        }
    }
}

@Composable
private fun EngineTab(uiState: PlayerUiState, viewModel: PlayerViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        StudioPanel {
            SectionHeading(
                overline = "ENGINE",
                title = "Control de reproduccion",
                body = stringResource(R.string.studio_engine_info)
            )
        }

        ResetCard(onReset = viewModel::resetAudioStudio)

        StudioPanel {
            LabeledSlider(
                label = stringResource(R.string.studio_speed_label),
                value = uiState.speed,
                valueRange = 0.5f..2.0f,
                valueText = String.format(Locale.US, "%.2fx", uiState.speed),
                onValueChange = viewModel::setSpeed
            )
        }

        StudioPanel {
            LabeledSlider(
                label = stringResource(R.string.studio_pitch_label),
                value = uiState.pitch,
                valueRange = 0.5f..2.0f,
                valueText = String.format(Locale.US, "%.2fx", uiState.pitch),
                onValueChange = viewModel::setPitch
            )
        }
    }
}

@Composable
private fun EqualizerTab(uiState: PlayerUiState, viewModel: PlayerViewModel) {
    if (!uiState.audioEffectsEnabled) {
        DisabledEffectsCard(onEnable = { viewModel.setAudioEffectsEnabled(true) })
        return
    }

    if (!uiState.effectsAvailable || uiState.eqBands.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.studio_eq_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        StudioPanel {
            SectionHeading(
                overline = "EQ CONSOLE",
                title = stringResource(R.string.studio_eq_title),
                body = "Ajuste fino por banda con referencia central y recorrido preciso."
            )
        }

        PresetRow(
            onPresetClick = { preset -> viewModel.applyEqPreset(preset) },
            onReset = viewModel::resetAudioStudio
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            tonalElevation = 12.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                        )
                    )
                    .padding(vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MASTER BOARD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "10 BANDS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    items(uiState.eqBands) { band ->
                        val freqKhz = band.centerFreqHz / 1000f
                        val label = if (freqKhz >= 1f) {
                            String.format(Locale.US, "%.1fk", freqKhz)
                        } else {
                            "${band.centerFreqHz}"
                        }

                        Box(
                            modifier = Modifier
                                .width(72.dp)
                                .height(320.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            StudioFader(
                                value = band.level.toFloat(),
                                valueRange = band.minLevel.toFloat()..band.maxLevel.toFloat(),
                                onValueChange = { viewModel.setEqBandLevel(band.index, it.toInt()) },
                                label = label,
                                valueText = "${band.level / 100}dB"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EffectsTab(uiState: PlayerUiState, viewModel: PlayerViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        when {
            !uiState.audioEffectsEnabled -> DisabledEffectsCard(onEnable = { viewModel.setAudioEffectsEnabled(true) })
            !uiState.effectsAvailable -> InfoCard(text = stringResource(R.string.studio_effects_unavailable))
            else -> InfoCard(text = stringResource(R.string.studio_effects_info))
        }

        EffectControlCard(
            title = stringResource(R.string.studio_bass_title),
            subtitle = stringResource(R.string.studio_bass_subtitle),
            value = uiState.bassStrength.toFloat(),
            valueRange = 0f..1000f,
            valueText = "${uiState.bassStrength / 10}%",
            onValueChange = { viewModel.setBassStrength(it.toInt()) },
            icon = Icons.Default.Audiotrack,
            enabled = uiState.audioEffectsEnabled && uiState.effectsAvailable
        )

        EffectControlCard(
            title = stringResource(R.string.studio_virtualizer_title),
            subtitle = stringResource(R.string.studio_virtualizer_subtitle),
            value = uiState.virtualizerStrength.toFloat(),
            valueRange = 0f..1000f,
            valueText = "${uiState.virtualizerStrength / 10}%",
            onValueChange = { viewModel.setVirtualizerStrength(it.toInt()) },
            icon = Icons.Default.SurroundSound,
            enabled = uiState.audioEffectsEnabled && uiState.effectsAvailable
        )

        EffectControlCard(
            title = stringResource(R.string.studio_loudness_title),
            subtitle = stringResource(R.string.studio_loudness_subtitle),
            value = uiState.loudnessGainMb.toFloat(),
            valueRange = -1500f..3000f,
            valueText = "${uiState.loudnessGainMb / 100} dB",
            onValueChange = { viewModel.setLoudnessGain(it.toInt()) },
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            enabled = uiState.audioEffectsEnabled && uiState.effectsAvailable
        )
    }
}

@Composable
private fun LyricsTab(uiState: PlayerUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        InfoCard(text = stringResource(R.string.studio_lyrics_info))

        StudioPanel {
            SectionHeading(
                overline = "LYRICS",
                title = stringResource(R.string.studio_lyrics_title),
                body = if (uiState.currentTrack != null) {
                    stringResource(R.string.studio_lyrics_body_active, uiState.currentTrack.title)
                } else {
                    stringResource(R.string.studio_lyrics_body_idle)
                }
            )
        }
    }
}

@Composable
private fun PresetRow(
    onPresetClick: (List<Int>) -> Unit,
    onReset: () -> Unit
) {
    val presets = listOf(
        stringResource(R.string.studio_preset_flat) to listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        stringResource(R.string.studio_preset_bass) to listOf(600, 500, 350, 150, 0, -100, -200, -250, -300, -350),
        stringResource(R.string.studio_preset_vocal) to listOf(-200, -150, 0, 250, 400, 450, 350, 200, 100, 0),
        stringResource(R.string.studio_preset_bright) to listOf(-250, -200, -100, 0, 100, 250, 350, 450, 500, 550)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(presets) { (label, values) ->
            OutlinedButton(onClick = { onPresetClick(values) }) {
                Text(label)
            }
        }
        item {
            OutlinedButton(onClick = onReset) {
                Text(stringResource(R.string.studio_reset_title))
            }
        }
    }
}

@Composable
private fun DisabledEffectsCard(
    onEnable: () -> Unit
) {
    StudioPanel(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.studio_safe_mode_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = stringResource(R.string.studio_safe_mode_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onEnable) {
                Text(stringResource(R.string.studio_enable_effects))
            }
        }
    }
}

@Composable
private fun ResetCard(
    onReset: () -> Unit
) {
    StudioPanel {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.studio_reset_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = stringResource(R.string.studio_reset_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = onReset) {
                Text(stringResource(R.string.action_done))
            }
        }
    }
}

@Composable
private fun EffectControlCard(
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
    onValueChange: (Float) -> Unit,
    icon: ImageVector,
    enabled: Boolean
) {
    StudioPanel {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (enabled) {
                LabeledSlider(
                    label = "",
                    value = value,
                    valueRange = valueRange,
                    valueText = valueText,
                    onValueChange = onValueChange
                )
            } else {
                Text(
                    text = stringResource(R.string.studio_effect_disabled_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StudioPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        tonalElevation = 10.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun StudioPill(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SectionHeading(
    overline: String,
    title: String,
    body: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = overline,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
