package com.limbe.hexamusicplayer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.limbe.hexamusicplayer.infrastructure.service.PlaybackMediaSessionService
import com.limbe.hexamusicplayer.ui.navigation.AppRoute
import com.limbe.hexamusicplayer.ui.screens.library.LibraryViewModel
import com.limbe.hexamusicplayer.ui.screens.player.PlayerViewModel

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
    var hasAskedNotificationPermission by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        if (playerUiState.isPlaying) {
            PlaybackMediaSessionService.start(context.applicationContext)
        }
    }

    LaunchedEffect(playerUiState.isPlaying, playerUiState.currentTrack != null) {
        if (playerUiState.isPlaying) {
            val shouldRequestNotifications = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED

            if (shouldRequestNotifications && !hasAskedNotificationPermission) {
                hasAskedNotificationPermission = true
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                PlaybackMediaSessionService.start(context.applicationContext)
            }
        } else if (playerUiState.currentTrack == null) {
            PlaybackMediaSessionService.stop(context.applicationContext)
        }
    }

    AppChromeScaffold(
        currentRoute = currentRoute,
        playerUiState = playerUiState,
        onPlayPause = playerViewModel::togglePlayback,
        onOpenPlayer = { navController.navigate(AppRoute.PLAYER) },
        onNavigate = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    ) { paddingValues ->
        AppNavGraph(
            navController = navController,
            libraryViewModel = libraryViewModel,
            playerViewModel = playerViewModel,
            playerUiState = playerUiState,
            modifier = Modifier.padding(
                bottom = if (
                    currentRoute == AppRoute.PLAYER ||
                    currentRoute == AppRoute.SETTINGS ||
                    currentRoute == AppRoute.STUDIO
                ) {
                    0.dp
                } else {
                    paddingValues.calculateBottomPadding()
                }
            )
        )
    }
}
