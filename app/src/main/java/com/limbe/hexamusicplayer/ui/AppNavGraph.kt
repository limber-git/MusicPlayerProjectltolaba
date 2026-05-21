package com.limbe.hexamusicplayer.ui

import androidx.compose.animation.AnimatedContentTransitionScope
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
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) +
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) +
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
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
        composable(AppRoute.STUDIO) {
            StudioScreen(viewModel = playerViewModel)
        }
        composable(
            route = AppRoute.PLAYER,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(400))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(400))
            },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(400))
            }
        ) {
            PlayerScreen(
                viewModel = playerViewModel,
                onBack = { navController.popBackStack() },
                onOpenStudio = { navController.navigate(AppRoute.STUDIO) }
            )
        }
        composable(AppRoute.SETTINGS) {
            SettingsScreen(
                playerViewModel = playerViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
