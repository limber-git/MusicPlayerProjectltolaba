package com.limbe.hexamusicplayer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.limbe.hexamusicplayer.infrastructure.service.PlaybackMediaSessionService
import com.limbe.hexamusicplayer.ui.components.HexaBottomNavigationBar
import com.limbe.hexamusicplayer.ui.components.MiniPlayer
import com.limbe.hexamusicplayer.ui.screens.library.LibraryScreen
import com.limbe.hexamusicplayer.ui.screens.library.LibraryViewModel
import com.limbe.hexamusicplayer.ui.screens.player.PlayerScreen
import com.limbe.hexamusicplayer.ui.screens.player.PlayerViewModel
import com.limbe.hexamusicplayer.ui.screens.studio.StudioScreen
import com.limbe.hexamusicplayer.ui.screens.explorer.ExplorerScreen
import com.limbe.hexamusicplayer.ui.screens.settings.SettingsScreen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween

@Composable
fun HexaApp(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    val playerUiState by playerViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(playerUiState.currentTrack?.id, playerUiState.isPlaying) {
        if (playerUiState.currentTrack != null || playerUiState.isPlaying) {
            PlaybackMediaSessionService.start(context.applicationContext)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute != "player" && currentRoute != "settings") {
                Column {
                    if (playerUiState.currentTrack != null) {
                        MiniPlayer(
                            uiState = playerUiState,
                            onPlayPause = playerViewModel::togglePlayback,
                            onClick = { navController.navigate("player") }
                        )
                    }
                    HexaBottomNavigationBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "library",
            modifier = Modifier.padding(bottom = if (currentRoute == "player" || currentRoute == "settings") 0.dp else paddingValues.calculateBottomPadding()),
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
        ) {
            composable("library") {
                LibraryScreen(
                    viewModel = libraryViewModel,
                    currentTrackId = playerUiState.currentTrack?.id,
                    isPlaying = playerUiState.isPlaying,
                    onTrackClick = { track, queue ->
                        playerViewModel.playTrack(track, queue)
                    },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable("explorer") {
                ExplorerScreen(
                    viewModel = libraryViewModel,
                    onAlbumClick = { track, queue ->
                        playerViewModel.playTrack(track, queue)
                    }
                )
            }
            composable("studio") {
                StudioScreen(viewModel = playerViewModel)
            }
            composable(
                route = "player",
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(400)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(400)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(400)) }
            ) {
                PlayerScreen(
                    viewModel = playerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    playerViewModel = playerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
