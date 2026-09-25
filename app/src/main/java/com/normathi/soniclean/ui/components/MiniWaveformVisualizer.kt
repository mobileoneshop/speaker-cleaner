package com.normathi.soniclean.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * Mini embedded waveform visualizer for the Auto Clean hero card.
 * Features a gentle idle "breathing" animation with FastOutSlowInEasing per brain/ANIMATIONS.md.
 */
@Composable
fun MiniWaveformVisualizer(
    modifier: Modifier = Modifier,
    barCount: Int = 28,
    height: Dp = 44.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "idle_breathing_transition")

    // Gentle breathing phase cycle (2200ms) with FastOutSlowInEasing
    val breathPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "breath_phase"
    )

    // Breathing amplitude modulation (0.4f to 1.0f)
    val breathAmplitude by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_amplitude"
    )

    val gradientBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f),
                Color.White.copy(alpha = 0.35f)
            )
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .semantics { contentDescription = "Mini Waveform Visualizer" }
            .testTag("mini_waveform_visualizer")
    ) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barSpacing = totalWidth / barCount
        val barWidth = (barSpacing * 0.60f).coerceAtLeast(2f)
        val cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        val minHeight = 4.dp.toPx()
        val maxHeight = canvasHeight * 0.92f

        for (i in 0 until barCount) {
            // Symmetrical gentle undulating wave centered on the visualizer
            val normalizedIdx = (i - barCount / 2f) / (barCount / 2f)
            val bellCurve = (1f - normalizedIdx * normalizedIdx).coerceIn(0.2f, 1f)

            val wave = sin(breathPhase + i * 0.28f)
            val normalizedWave = 0.4f + 0.6f * ((wave + 1f) / 2f)

            val barHeight = minHeight + (maxHeight - minHeight) * bellCurve * normalizedWave * breathAmplitude
            val x = i * barSpacing + (barSpacing - barWidth) / 2f
            val y = (canvasHeight - barHeight) / 2f

            drawRoundRect(
                brush = gradientBrush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = cornerRadius
            )
        }
    }
}
