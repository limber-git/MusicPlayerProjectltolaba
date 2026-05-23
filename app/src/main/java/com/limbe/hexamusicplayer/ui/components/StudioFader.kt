package com.limbe.hexamusicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StudioFader(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    label: String,
    valueText: String,
    modifier: Modifier = Modifier
) {
    val normalizedValue = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)
    val zeroValue = 0f.coerceIn(valueRange.start, valueRange.endInclusive)
    val zeroRatio = ((zeroValue - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)

    Column(
        modifier = modifier.width(68.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        FaderTrack(
            valueRatio = normalizedValue,
            zeroRatio = zeroRatio,
            onRatioChange = { ratio ->
                onValueChange(valueRange.start + ratio * (valueRange.endInclusive - valueRange.start))
            }
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FaderTrack(
    valueRatio: Float,
    zeroRatio: Float,
    onRatioChange: (Float) -> Unit
) {
    val trackHeight = 228.dp
    val knobHeight = 34.dp
    val corner = RoundedCornerShape(18.dp)

    BoxWithConstraints(
        modifier = Modifier
            .width(58.dp)
            .height(trackHeight)
            .clip(corner)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                shape = corner
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onRatioChange((1f - offset.y / size.height).coerceIn(0f, 1f))
                    }
                ) { change, _ ->
                    onRatioChange((1f - change.position.y / size.height).coerceIn(0f, 1f))
                }
            }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        val travel = maxHeight - knobHeight
        val knobOffset = travel * (1f - valueRatio)
        val indicatorTop = minOf(valueRatio, zeroRatio)
        val indicatorBottom = maxOf(valueRatio, zeroRatio)

        GridGuide(modifier = Modifier.align(Alignment.TopCenter))
        GridGuide(modifier = Modifier.align(Alignment.Center))
        GridGuide(modifier = Modifier.align(Alignment.BottomCenter))

        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .align(Alignment.Center)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .fillMaxHeight((indicatorBottom - indicatorTop).coerceAtLeast(0.012f))
                .offset(y = maxHeight * (1f - indicatorBottom))
                .clip(RoundedCornerShape(999.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.82f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = knobOffset)
                .fillMaxWidth()
                .height(knobHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color(0xFFE8EDF7),
                            Color(0xFFC4CDD9)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(2.dp)
                    .background(Color.Black.copy(alpha = 0.2f), CircleShape)
            )
        }
    }
}

@Composable
private fun GridGuide(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    )
}
