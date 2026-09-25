package com.normathi.soniclean.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Dark Palette (Default)
val DarkBackground = Color(0xFF0B0F1A)
val DarkSurface = Color(0xFF141B2E)
val DarkSurfaceHigh = Color(0xFF1C2540)
val DarkTextPrimary = Color(0xFFF2F5FA)
val DarkTextSecondary = Color(0xFF93A0B8)
val DarkAccent = Color(0xFF00E5FF)
val DarkAccentSecondary = Color(0xFF7C5CFF)

// Light Palette
val LightBackground = Color(0xFFF4F7FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceHigh = Color(0xFFEAF0F8)
val LightTextPrimary = Color(0xFF0E1B2E)
val LightTextSecondary = Color(0xFF5B6B84)
val LightAccent = Color(0xFF0090B3)
val LightAccentSecondary = Color(0xFF635BFF)

// Semantic Colors
val SuccessGreen = Color(0xFF34D399)
val WarningYellow = Color(0xFFFBBF24)
val DangerRed = Color(0xFFF87171)

// Gradients
val AccentGradient = Brush.horizontalGradient(
    colors = listOf(DarkAccent, DarkAccentSecondary)
)
val AccentGradientVertical = Brush.verticalGradient(
    colors = listOf(DarkAccent, DarkAccentSecondary)
)
