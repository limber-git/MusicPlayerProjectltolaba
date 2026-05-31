package com.limbe.hexamusicplayer.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.limbe.hexamusicplayer.ui.navigation.AppRoute
import com.limbe.hexamusicplayer.ui.screens.explorer.ExplorerScreen
import com.limbe.hexamusicplayer.ui.screens.home.HomeScreen
import com.limbe.hexamusicplayer.ui.screens.library.LibraryScreen
import com.limbe.hexamusicplayer.ui.screens.library.LibraryViewModel
import com.limbe.hexamusicplayer.ui.screens.player.PlayerScreen
import com.limbe.hexamusicplayer.ui.screens.player.PlayerUiState
import com.limbe.hexamusicplayer.ui.screens.player.PlayerViewModel
import com.limbe.hexamusicplayer.ui.screens.search.SearchScreen
import com.limbe.hexamusicplayer.ui.screens.settings.SettingsScreen
import com.limbe.hexamusicplayer.ui.screens.studio.StudioScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    playerUiState: PlayerUiState
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.HOME,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) +
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) +
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300, easing = FastOutLinearInEasing)
                )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) +
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) +
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300, easing = FastOutLinearInEasing)
                )
        }
    ) {
        composable(AppRoute.HOME) {
            HomeScreen(
                libraryViewModel = libraryViewModel,
                playerUiState = playerUiState,
                onTrackClick = playerViewModel::playTrack,
                onTrackPlayNext = playerViewModel::playNext,
                onTrackAddToQueue = playerViewModel::addToQueue,
                onOpenPlayer = { navController.navigate(AppRoute.PLAYER) },
                onOpenStudio = { navController.navigate(AppRoute.STUDIO) },
                onOpenExplorer = {
                    navController.navigate(AppRoute.EXPLORER) {
                        launchSingleTop = true
                    }
                },
                onOpenSettings = { navController.navigate(AppRoute.SETTINGS) }
            )
        }
        composable(AppRoute.LIBRARY) {
            LibraryScreen(
                viewModel = libraryViewModel,
                currentTrackId = playerUiState.currentTrack?.id,
                isPlaying = playerUiState.isPlaying,
                onTrackClick = playerViewModel::playTrack,
                onTrackPlayNext = playerViewModel::playNext,
                onTrackAddToQueue = playerViewModel::addToQueue,
                onSettingsClick = { navController.navigate(AppRoute.SETTINGS) }
            )
        }
        composable(AppRoute.EXPLORER) {
            ExplorerScreen(
                viewModel = libraryViewModel,
                onAlbumClick = playerViewModel::playTrack
            )
        }
        composable(AppRoute.SEARCH) {
            SearchScreen(
                libraryViewModel = libraryViewModel,
                playerUiState = playerUiState,
                onTrackClick = playerViewModel::playTrack,
                onTrackPlayNext = playerViewModel::playNext,
                onTrackAddToQueue = playerViewModel::addToQueue
            )
        }
        composable(
            route = AppRoute.STUDIO,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(400, easing = FastOutLinearInEasing)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(400, easing = FastOutLinearInEasing)
                )
            }
        ) {
            StudioScreen(viewModel = playerViewModel)
        }
        composable(
            route = AppRoute.PLAYER,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(400, easing = FastOutLinearInEasing)
                )
            },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(400, easing = FastOutLinearInEasing)
                )
            }
        ) {
            PlayerScreen(
                viewModel = playerViewModel,
                onBack = { navController.popBackStack() },
                onOpenStudio = { navController.navigate(AppRoute.STUDIO) }
            )
        }
        composable(
            route = AppRoute.SETTINGS,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350, easing = FastOutLinearInEasing)
                ) + fadeOut(animationSpec = tween(350))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350, easing = FastOutLinearInEasing)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) {
            SettingsScreen(
                playerViewModel = playerViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
