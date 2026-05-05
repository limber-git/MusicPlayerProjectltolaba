package com.limbe.hexamusicplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF091124),
                        Color(0xFF0D1D3B),
                        Color(0xFF121A2F)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Column {
                    if (playerUiState.currentTrack != null && currentRoute != "player") {
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
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "library",
                modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                composable("library") {
                    LibraryScreen(
                        viewModel = libraryViewModel,
                        currentTrackId = playerUiState.currentTrack?.id,
                        isPlaying = playerUiState.isPlaying,
                        onTrackClick = { track ->
                            playerViewModel.playTrack(track)
                        }
                    )
                }
                composable("explorer") {
                    ExplorerScreen()
                }
                composable("studio") {
                    StudioScreen(viewModel = playerViewModel)
                }
                composable("player") {
                    PlayerScreen(viewModel = playerViewModel)
                }
            }
        }
    }
}
