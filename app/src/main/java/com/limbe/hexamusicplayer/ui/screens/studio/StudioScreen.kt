package com.limbe.hexamusicplayer.ui.screens.studio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.limbe.hexamusicplayer.ui.components.LabeledSlider
import com.limbe.hexamusicplayer.ui.screens.player.PlayerUiState
import com.limbe.hexamusicplayer.ui.screens.player.PlayerViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    viewModel: PlayerViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Text(
                        text = "AUDIO STUDIO",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                ControlStudioCard(
                    uiState = uiState,
                    onSetSpeed = viewModel::setSpeed,
                    onSetPitch = viewModel::setPitch
                )
            }

            item {
                EqualizerCard(
                    uiState = uiState,
                    onBandLevelChange = viewModel::setEqBandLevel,
                    onBassStrengthChange = viewModel::setBassStrength,
                    onVirtualizerStrengthChange = viewModel::setVirtualizerStrength,
                    onLoudnessGainChange = viewModel::setLoudnessGain
                )
            }

            item {
                Box(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun ControlStudioCard(
    uiState: PlayerUiState,
    onSetSpeed: (Float) -> Unit,
    onSetPitch: (Float) -> Unit
) {
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
                    text = "Motor de Audio",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

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
                label = "Presets de Velocidad",
                onPick = onSetSpeed
            )

            LabeledSlider(
                label = "Tonalidad (Pitch)",
                value = uiState.pitch,
                valueRange = 0.5f..2.0f,
                valueText = String.format(Locale.US, "%.2fx", uiState.pitch),
                onValueChange = onSetPitch
            )
        }
    }
}

@Composable
private fun EqualizerCard(
    uiState: PlayerUiState,
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
                    text = "Ecualizador de 10 Bandas",
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
                            text = "Reproduce música para activar el procesado avanzado.",
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
                            label = "Refuerzo de Graves (Bass)",
                            value = uiState.bassStrength.toFloat(),
                            valueRange = 0f..1000f,
                            valueText = "${uiState.bassStrength}",
                            onValueChange = { onBassStrengthChange(it.toInt()) }
                        )

                        LabeledSlider(
                            label = "Virtualizador 3D",
                            value = uiState.virtualizerStrength.toFloat(),
                            valueRange = 0f..1000f,
                            valueText = "${uiState.virtualizerStrength}",
                            onValueChange = { onVirtualizerStrengthChange(it.toInt()) }
                        )

                        LabeledSlider(
                            label = "Ganancia de Salida (Loudness)",
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
