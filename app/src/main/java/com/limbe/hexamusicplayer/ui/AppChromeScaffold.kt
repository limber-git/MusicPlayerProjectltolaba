package com.limbe.hexamusicplayer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.limbe.hexamusicplayer.ui.components.HexaBottomNavigationBar
import com.limbe.hexamusicplayer.ui.components.MiniPlayer
import com.limbe.hexamusicplayer.ui.navigation.AppRoute
import com.limbe.hexamusicplayer.ui.screens.player.PlayerUiState

@Composable
fun AppChromeScaffold(
    currentRoute: String?,
    playerUiState: PlayerUiState,
    onPlayPause: () -> Unit,
    onOpenPlayer: () -> Unit,
    onNavigate: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute != AppRoute.PLAYER && currentRoute != AppRoute.SETTINGS && currentRoute != AppRoute.STUDIO) {
                Column {
                    if (playerUiState.currentTrack != null) {
                        MiniPlayer(
                            uiState = playerUiState,
                            onPlayPause = onPlayPause,
                            onClick = onOpenPlayer
                        )
                    }
                    HexaBottomNavigationBar(
                        currentRoute = currentRoute,
                        onNavigate = onNavigate
                    )
                }
            }
        },
        content = content
    )
}
