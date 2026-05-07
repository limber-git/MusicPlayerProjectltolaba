package com.limbe.hexamusicplayer.ui.screens.studio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    var selectedTab by remember { mutableIntStateOf(1) } // Default to EQ

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                    title = {
                        Text(
                            text = "Audio Studio",
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
                        text = { Text("Motor", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Equalizador", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Efectos", fontSize = 12.sp) },
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        InfoCard(text = "Controla la velocidad y el tono de la reproducción en tiempo real sin perder calidad.")
        
        LabeledSlider(
            label = "Velocidad de Reproducción",
            value = uiState.speed,
            valueRange = 0.5f..2.0f,
            valueText = String.format(Locale.US, "%.2fx", uiState.speed),
            onValueChange = viewModel::setSpeed
        )

        LabeledSlider(
            label = "Tonalidad (Pitch)",
            value = uiState.pitch,
            valueRange = 0.5f..2.0f,
            valueText = String.format(Locale.US, "%.2fx", uiState.pitch),
            onValueChange = viewModel::setPitch
        )
    }
}

@Composable
private fun EqualizerTab(uiState: PlayerUiState, viewModel: PlayerViewModel) {
    if (uiState.attachedSessionId == null || uiState.eqBands.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Reproduce música para activar el EQ Pro",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Console EQ - 10 Bands",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primary
            )
            
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(uiState.eqBands) { band ->
                    val freqKhz = band.centerFreqHz / 1000f
                    val label = if (freqKhz >= 1f) String.format(Locale.US, "%.1fk", freqKhz) else "${band.centerFreqHz}"
                    
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

@Composable
private fun EffectsTab(uiState: PlayerUiState, viewModel: PlayerViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (uiState.attachedSessionId == null) {
            InfoCard(text = "Los efectos de post-procesado requieren que una pista esté sonando.")
        }

        EffectControlCard(
            title = "Bass Boost",
            subtitle = "Refuerza las frecuencias bajas",
            value = uiState.bassStrength.toFloat(),
            valueRange = 0f..1000f,
            valueText = "${uiState.bassStrength / 10}%",
            onValueChange = { viewModel.setBassStrength(it.toInt()) },
            icon = Icons.Default.Audiotrack
        )

        EffectControlCard(
            title = "Virtualizador 3D",
            subtitle = "Simulación de audio espacial",
            value = uiState.virtualizerStrength.toFloat(),
            valueRange = 0f..1000f,
            valueText = "${uiState.virtualizerStrength / 10}%",
            onValueChange = { viewModel.setVirtualizerStrength(it.toInt()) },
            icon = Icons.Default.SurroundSound
        )

        EffectControlCard(
            title = "Loudness Enhancer",
            subtitle = "Ganancia de salida maestra",
            value = uiState.loudnessGainMb.toFloat(),
            valueRange = -1500f..3000f,
            valueText = "${uiState.loudnessGainMb / 100} dB",
            onValueChange = { viewModel.setLoudnessGain(it.toInt()) },
            icon = Icons.AutoMirrored.Filled.VolumeUp
        )
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
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            LabeledSlider(
                label = "",
                value = value,
                valueRange = valueRange,
                valueText = valueText,
                onValueChange = onValueChange
            )
        }
    }
}

@Composable
private fun InfoCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
