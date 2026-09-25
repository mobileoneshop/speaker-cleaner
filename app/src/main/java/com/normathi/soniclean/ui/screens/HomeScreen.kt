package com.normathi.soniclean.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.normathi.soniclean.R
import com.normathi.soniclean.ui.components.HomeStatsRow
import com.normathi.soniclean.ui.components.MiniWaveformVisualizer
import com.normathi.soniclean.ui.components.SafetyBanner
import com.normathi.soniclean.ui.components.TipsCarouselCard
import com.normathi.soniclean.ui.theme.AccentGradient
import com.normathi.soniclean.ui.theme.DangerRed
import com.normathi.soniclean.ui.theme.DarkAccent
import com.normathi.soniclean.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onNavigateToAutoClean: () -> Unit = {},
    onNavigateToWaterEject: () -> Unit = {},
    onNavigateToManual: () -> Unit = {},
    onNavigateToVibration: () -> Unit = {},
    onNavigateToSoundTest: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Stop Quick Clean immediately if leaving this screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopQuickClean()
        }
    }

    // Wrap navigation handlers to safely terminate Quick Clean
    val safeNavigateToAutoClean = {
        viewModel.stopQuickClean()
        onNavigateToAutoClean()
    }
    val safeNavigateToWaterEject = {
        viewModel.stopQuickClean()
        onNavigateToWaterEject()
    }
    val safeNavigateToManual = {
        viewModel.stopQuickClean()
        onNavigateToManual()
    }
    val safeNavigateToVibration = {
        viewModel.stopQuickClean()
        onNavigateToVibration()
    }
    val safeNavigateToSoundTest = {
        viewModel.stopQuickClean()
        onNavigateToSoundTest()
    }
    val safeNavigateToHistory = {
        viewModel.stopQuickClean()
        onNavigateToHistory()
    }
    val safeNavigateToSettings = {
        viewModel.stopQuickClean()
        onNavigateToSettings()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = DarkAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(end = 14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                .clickable(onClick = safeNavigateToHistory)
                                .testTag("history_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = stringResource(R.string.history_title),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                .clickable(onClick = safeNavigateToSettings)
                                .testTag("settings_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings_title),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .testTag("home_lazy_column"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Prominent "Clean Your Speaker" title header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.home_subtitle),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Headphone safety banner if warning is active
            if (uiState.headphoneWarning != null) {
                item {
                    SafetyBanner(
                        message = uiState.headphoneWarning ?: "",
                        onDismiss = { viewModel.dismissHeadphoneWarning() }
                    )
                }
            }

            // 2. Auto Clean hero card with embedded mini WaveformVisualizer
            item {
                Card(
                    onClick = safeNavigateToAutoClean,
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("hero_auto_clean_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AccentGradient)
                            .padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.mode_auto),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.mode_auto_sub),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.22f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = stringResource(R.string.mode_auto),
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            MiniWaveformVisualizer(
                                modifier = Modifier.fillMaxWidth(),
                                height = 44.dp
                            )
                        }
                    }
                }
            }

            // 3. Stats row below the hero (Total cleans, This week, Last cleaned)
            item {
                HomeStatsRow(
                    totalCleans = uiState.totalCleans,
                    thisWeekCleans = uiState.thisWeekCleans,
                    lastCleaned = uiState.lastCleaned
                )
            }

            // 4. "Quick Clean (10–1000 Hz)" secondary button with live countdown and audio frequency sweep
            item {
                OutlinedButton(
                    onClick = { viewModel.onQuickCleanTapped() },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = if (uiState.isQuickCleaning) DangerRed else DarkAccent
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (uiState.isQuickCleaning) DangerRed.copy(alpha = 0.12f) else DarkAccent.copy(alpha = 0.08f),
                        contentColor = if (uiState.isQuickCleaning) DangerRed else DarkAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("quick_clean_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.isQuickCleaning) Icons.Default.Stop else Icons.Default.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isQuickCleaning) {
                                stringResource(
                                    R.string.quick_clean_countdown,
                                    uiState.quickCleanRemainingSec,
                                    uiState.currentQuickCleanFreq
                                )
                            } else {
                                stringResource(
                                    R.string.quick_clean_btn,
                                    uiState.quickCleanTotalDurationSec
                                )
                            },
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            // Mode: Water Eject
            item {
                HomeModeItem(
                    title = stringResource(R.string.mode_water_eject),
                    subtitle = stringResource(R.string.mode_water_eject_sub),
                    icon = Icons.Default.WaterDrop,
                    testTag = "mode_water_eject_card",
                    onClick = safeNavigateToWaterEject
                )
            }

            // Mode: Manual
            item {
                HomeModeItem(
                    title = stringResource(R.string.mode_manual),
                    subtitle = stringResource(R.string.mode_manual_sub),
                    icon = Icons.Default.Speed,
                    testTag = "mode_manual_card",
                    onClick = safeNavigateToManual
                )
            }

            // Mode: Vibration
            item {
                HomeModeItem(
                    title = stringResource(R.string.mode_vibration),
                    subtitle = stringResource(R.string.mode_vibration_sub),
                    icon = Icons.Default.Vibration,
                    testTag = "mode_vibration_card",
                    onClick = safeNavigateToVibration
                )
            }

            // Mode: Sound Test
            item {
                HomeModeItem(
                    title = stringResource(R.string.mode_test),
                    subtitle = stringResource(R.string.mode_test_sub),
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    testTag = "mode_test_card",
                    onClick = safeNavigateToSoundTest
                )
            }

            // 5. Tips carousel card at the bottom
            item {
                TipsCarouselCard()
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HomeModeItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DarkAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = DarkAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
