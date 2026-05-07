package com.limbe.hexamusicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VerticalFader(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var height by remember { mutableStateOf(0f) }
    val density = LocalDensity.current.density
    
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Box(
            modifier = Modifier
                .width(40.dp)
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .onGloballyPositioned { height = it.size.height.toFloat() }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val position = change.position.y.coerceIn(0f, height)
                        val ratio = 1f - (position / height)
                        val newValue = valueRange.start + ratio * (valueRange.endInclusive - valueRange.start)
                        onValueChange(newValue)
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Track background line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )
            
            // Level Indicator
            val ratio = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(ratio.coerceIn(0f, 1f))
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        )
                    )
            )
            
            // Fader Knob
            Box(
                modifier = Modifier
                    .offset(y = ( (1f - ratio) * (height / density) ).dp - (height / density).dp ) // This offset logic is tricky in Compose without careful state
                    // Re-simplifying for robustness:
                    .align(Alignment.TopCenter)
                    .padding(top = ((1f - ratio.coerceIn(0f, 1f)) * 180).dp) // Fixed height approximation for now or use BoxWithConstraints
                    .size(width = 32.dp, height = 12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
            )
        }
    }
}

// A more robust version using built-in Slider but rotated? 
// No, let's go with a custom one that looks like a studio fader.
@Composable
fun StudioFader(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    label: String,
    valueText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.height(240.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .width(48.dp)
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .pointerInput(valueRange) {
                    detectDragGestures { change, _ ->
                        val ratio = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                        onValueChange(valueRange.start + ratio * (valueRange.endInclusive - valueRange.start))
                    }
                }
        ) {
            val ratio = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
            
            // Progress
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(ratio.coerceIn(0.01f, 1f))
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            )
            
            // Line
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )
            
            // Knob
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-ratio * 200).dp) // Approximate height for the fader track
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.width(20.dp).height(2.dp).background(Color.Black.copy(alpha = 0.2f)))
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
