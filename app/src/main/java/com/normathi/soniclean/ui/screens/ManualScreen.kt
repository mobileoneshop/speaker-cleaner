package com.normathi.soniclean.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.normathi.soniclean.R
import com.normathi.soniclean.engine.audio.AudioConfig
import com.normathi.soniclean.engine.audio.AudioOutputTarget
import com.normathi.soniclean.ui.components.AppTopBar
import com.normathi.soniclean.ui.components.SafetyBanner
import com.normathi.soniclean.ui.components.WaveformVisualizer
import com.normathi.soniclean.ui.theme.AccentGradient
import com.normathi.soniclean.ui.theme.DangerRed
import com.normathi.soniclean.ui.theme.DarkAccent
import com.normathi.soniclean.ui.theme.SuccessGreen
import com.normathi.soniclean.viewmodel.ManualViewModel
import java.util.Locale

@Composable
fun ManualScreen(
    modifier: Modifier = Modifier,
    viewModel: ManualViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPlaying(recordHistory = true)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.mode_manual),
                onNavigateBack = {
                    viewModel.stopPlaying(recordHistory = true)
                    onNavigateBack()
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxSize()
            .testTag("manual_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Safety banner if warnings exist
            if (uiState.headphoneWarning != null) {
                SafetyBanner(
                    message = uiState.headphoneWarning ?: "",
                    onDismiss = { viewModel.dismissHeadphoneWarning() }
                )
            } else if (uiState.noticeMessage != null) {
                SafetyBanner(
                    message = uiState.noticeMessage ?: "",
                    onDismiss = { viewModel.dismissNotice() }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Output Target Selector Tabs (Speaker / Earpiece)
            TabRow(
                selectedTabIndex = if (uiState.outputTarget == AudioOutputTarget.SPEAKER) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = DarkAccent,
                indicator = { tabPositions ->
                    val index = if (uiState.outputTarget == AudioOutputTarget.SPEAKER) 0 else 1
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                        color = DarkAccent
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("manual_output_tabs")
            ) {
                Tab(
                    selected = uiState.outputTarget == AudioOutputTarget.SPEAKER,
                    onClick = { viewModel.onToggleOutputTarget(AudioOutputTarget.SPEAKER) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.output_speaker))
                        }
                    }
                )
                Tab(
                    selected = uiState.outputTarget == AudioOutputTarget.EARPIECE,
                    onClick = { viewModel.onToggleOutputTarget(AudioOutputTarget.EARPIECE) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Hearing,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.output_earpiece))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Frequency Readout
            Text(
                text = stringResource(R.string.manual_freq, uiState.frequencyHz),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = DarkAccent,
                modifier = Modifier.testTag("frequency_readout")
            )

            // Resonant Frequency Characteristic Badge
            val is180Peak = uiState.frequencyHz == 180
            val is165Sweet = uiState.frequencyHz == 165
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when {
                    is180Peak -> SuccessGreen.copy(alpha = 0.15f)
                    is165Sweet -> DarkAccent.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                },
                border = when {
                    is180Peak -> BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f))
                    is165Sweet -> BorderStroke(1.dp, DarkAccent.copy(alpha = 0.4f))
                    else -> null
                },
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Text(
                    text = when {
                        is180Peak -> "★ 180 Hz · Resonant Acoustic Peak"
                        is165Sweet -> "★ 165 Hz · Calibrated Sweet Spot"
                        uiState.frequencyHz >= 1000 -> "High Frequency Ultrasonic Sweep (${uiState.frequencyHz} Hz)"
                        else -> "Acoustic Pressure Tuning (${uiState.frequencyHz} Hz)"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (is180Peak || is165Sweet) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = when {
                        is180Peak -> SuccessGreen
                        is165Sweet -> DarkAccent
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            Text(
                text = stringResource(R.string.manual_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Frequency Slider (10 Hz to 1000 Hz)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Slider(
                    value = uiState.frequencyHz.toFloat(),
                    onValueChange = { viewModel.onFrequencyChanged(it.toInt()) },
                    valueRange = AudioConfig.MIN_FREQ.toFloat()..AudioConfig.MAX_FREQ.toFloat(),
                    steps = 0,
                    colors = SliderDefaults.colors(
                        thumbColor = DarkAccent,
                        activeTrackColor = DarkAccent,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("frequency_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${AudioConfig.MIN_FREQ} Hz",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "180 Hz (Acoustic Peak)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = DarkAccent
                    )
                    Text(
                        text = "${AudioConfig.MAX_FREQ} Hz",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Preset Chips (Row 1: Low-Mid Frequencies)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val presetsRow1 = listOf(140, 165, 180, 250)
                presetsRow1.forEach { preset ->
                    val isSelected = uiState.frequencyHz == preset
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onFrequencyChanged(preset) },
                        label = {
                            Text(
                                text = when (preset) {
                                    180 -> "★ 180Hz"
                                    165 -> "★ 165Hz"
                                    else -> "${preset}Hz"
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkAccent.copy(alpha = 0.2f),
                            selectedLabelColor = DarkAccent
                        ),
                        modifier = Modifier.testTag("preset_chip_$preset")
                    )
                }
            }

            // Quick Preset Chips (Row 2: Higher Frequencies up to 1000Hz)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val presetsRow2 = listOf(350, 440, 600, 1000)
                presetsRow2.forEach { preset ->
                    val isSelected = uiState.frequencyHz == preset
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onFrequencyChanged(preset) },
                        label = {
                            Text(
                                text = "${preset}Hz",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkAccent.copy(alpha = 0.2f),
                            selectedLabelColor = DarkAccent
                        ),
                        modifier = Modifier.testTag("preset_chip_$preset")
                    )
                }
            }

            // Duration Selector Chips (15s, 30s, 45s, 60s)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = DarkAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Timer Duration",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${uiState.selectedDurationSeconds}s",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = DarkAccent
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val durations = listOf(15, 30, 45, 60)
                    durations.forEach { durationSec ->
                        val isSelected = uiState.selectedDurationSeconds == durationSec
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onDurationChanged(durationSec) },
                            label = {
                                Text(
                                    text = "${durationSec}s",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkAccent.copy(alpha = 0.22f),
                                selectedLabelColor = DarkAccent
                            ),
                            modifier = Modifier.testTag("manual_duration_chip_$durationSec")
                        )
                    }
                }
            }

            // Waveform visualizer
            WaveformVisualizer(
                frequencyHz = uiState.frequencyHz.toFloat(),
                isPlaying = uiState.isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            )

            // Timer display & countdown
            val elapsedMinutes = uiState.elapsedSeconds / 60
            val elapsedSecs = uiState.elapsedSeconds % 60
            val maxSecs = if (uiState.outputTarget == AudioOutputTarget.EARPIECE) {
                AudioConfig.EARPIECE_BURST_MAX_SEC
            } else {
                uiState.maxDurationSeconds
            }
            val maxMinutes = maxSecs / 60
            val maxSecsRemaining = maxSecs % 60

            Text(
                text = if (uiState.isPlaying) {
                    String.format(
                        Locale.US,
                        "%02d:%02d / %02d:%02d · %ds remaining",
                        elapsedMinutes,
                        elapsedSecs,
                        maxMinutes,
                        maxSecsRemaining,
                        uiState.remainingSeconds
                    )
                } else {
                    String.format(
                        Locale.US,
                        "00:00 / %02d:%02d (Set: %ds)",
                        maxMinutes,
                        maxSecsRemaining,
                        maxSecs
                    )
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = if (uiState.isPlaying) DarkAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("elapsed_timer_text")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Play / Stop Primary Button
            Button(
                onClick = { viewModel.onTogglePlay() },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isPlaying) DangerRed else Color.Transparent
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(CircleShape)
                    .then(
                        if (!uiState.isPlaying) Modifier.background(AccentGradient)
                        else Modifier
                    )
                    .testTag("manual_play_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isPlaying) {
                            stringResource(R.string.auto_stop)
                        } else {
                            stringResource(R.string.test_play)
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
