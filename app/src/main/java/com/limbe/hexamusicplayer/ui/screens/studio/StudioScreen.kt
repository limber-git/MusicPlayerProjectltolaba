package com.limbe.hexamusicplayer.ui.screens.studio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Column {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                    title = {
                        Text(
                            text = stringResource(R.string.studio_title),
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
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(stringResource(R.string.studio_tab_engine), fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(stringResource(R.string.studio_tab_equalizer), fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(stringResource(R.string.studio_tab_effects), fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Waves, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
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
            }
        }
    }
}

@Composable
private fun EngineTab(uiState: PlayerUiState, viewModel: PlayerViewModel) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        InfoCard(text = stringResource(R.string.studio_engine_info))
        ResetCard(onReset = viewModel::resetAudioStudio)

        LabeledSlider(
            label = stringResource(R.string.studio_speed_label),
            value = uiState.speed,
            valueRange = 0.5f..2.0f,
            valueText = String.format(Locale.US, "%.2fx", uiState.speed),
            onValueChange = viewModel::setSpeed
        )

        LabeledSlider(
            label = stringResource(R.string.studio_pitch_label),
            value = uiState.pitch,
            valueRange = 0.5f..2.0f,
            valueText = String.format(Locale.US, "%.2fx", uiState.pitch),
            onValueChange = viewModel::setPitch
        )
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

    androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.studio_eq_title),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.primary
        )

        PresetRow(
            onPresetClick = { preset -> viewModel.applyEqPreset(preset) },
            onReset = viewModel::resetAudioStudio
        )

        LazyRow(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(uiState.eqBands) { band ->
                val freqKhz = band.centerFreqHz / 1000f
                val label = if (freqKhz >= 1f) {
                    String.format(Locale.US, "%.1fk", freqKhz)
                } else {
                    "${band.centerFreqHz}"
                }

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

@Composable
private fun EffectsTab(uiState: PlayerUiState, viewModel: PlayerViewModel) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
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
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(1f)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                androidx.compose.foundation.layout.Column {
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
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
