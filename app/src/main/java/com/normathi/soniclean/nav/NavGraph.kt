package com.normathi.soniclean.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.normathi.soniclean.ui.screens.AutoCleanScreen
import com.normathi.soniclean.ui.screens.HistoryScreen
import com.normathi.soniclean.ui.screens.HomeScreen
import com.normathi.soniclean.ui.screens.ManualScreen
import com.normathi.soniclean.ui.screens.SettingsScreen
import com.normathi.soniclean.ui.screens.SoundTestScreen
import com.normathi.soniclean.ui.screens.SplashScreen
import com.normathi.soniclean.ui.screens.VibrationScreen
import com.normathi.soniclean.ui.screens.WaterEjectScreen

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultSlideEnter(): EnterTransition {
    return fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(300, easing = FastOutSlowInEasing),
            initialOffset = { 48 }
        )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultSlideExit(): ExitTransition {
    return fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(300, easing = FastOutSlowInEasing),
            targetOffset = { 48 }
        )
}

@Composable
fun SonicCleanNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoutes.SPLASH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            route = NavRoutes.SPLASH,
            exitTransition = {
                fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing))
            }
        ) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SPLASH) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = NavRoutes.HOME,
            enterTransition = {
                fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        initialOffset = { 48 }
                    )
            }
        ) {
            HomeScreen(
                onNavigateToAutoClean = {
                    navController.navigate(NavRoutes.AUTO_CLEAN)
                },
                onNavigateToWaterEject = {
                    navController.navigate(NavRoutes.WATER_EJECT)
                },
                onNavigateToManual = {
                    navController.navigate(NavRoutes.MANUAL)
                },
                onNavigateToVibration = {
                    navController.navigate(NavRoutes.VIBRATION)
                },
                onNavigateToSoundTest = {
                    navController.navigate(NavRoutes.SOUND_TEST)
                },
                onNavigateToHistory = {
                    navController.navigate(NavRoutes.HISTORY)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
                }
            )
        }

        composable(
            route = NavRoutes.WATER_EJECT,
            enterTransition = { defaultSlideEnter() },
            exitTransition = { defaultSlideExit() }
        ) {
            WaterEjectScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = NavRoutes.AUTO_CLEAN,
            enterTransition = { defaultSlideEnter() },
            exitTransition = { defaultSlideExit() }
        ) {
            AutoCleanScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = NavRoutes.MANUAL,
            enterTransition = { defaultSlideEnter() },
            exitTransition = { defaultSlideExit() }
        ) {
            ManualScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = NavRoutes.VIBRATION,
            enterTransition = { defaultSlideEnter() },
            exitTransition = { defaultSlideExit() }
        ) {
            VibrationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = NavRoutes.SOUND_TEST,
            enterTransition = { defaultSlideEnter() },
            exitTransition = { defaultSlideExit() }
        ) {
            SoundTestScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = NavRoutes.HISTORY,
            enterTransition = { defaultSlideEnter() },
            exitTransition = { defaultSlideExit() }
        ) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartCleaning = {
                    navController.navigate(NavRoutes.AUTO_CLEAN)
                }
            )
        }

        composable(
            route = NavRoutes.SETTINGS,
            enterTransition = { defaultSlideEnter() },
            exitTransition = { defaultSlideExit() }
        ) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
