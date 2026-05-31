package com.limbe.hexamusicplayer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.limbe.hexamusicplayer.ui.components.HexaBottomNavigationBar
import com.limbe.hexamusicplayer.ui.components.MiniPlayer
import com.limbe.hexamusicplayer.ui.navigation.AppRoute
import com.limbe.hexamusicplayer.ui.screens.player.PlayerUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun AppChromeScaffold(
    currentRoute: String?,
    playerUiState: PlayerUiState,
    positionFlow: StateFlow<Long>,
    onPlayPause: () -> Unit,
    onOpenPlayer: () -> Unit,
    onNavigate: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val isChromeVisible = currentRoute != AppRoute.PLAYER && 
                          currentRoute != AppRoute.SETTINGS && 
                          currentRoute != AppRoute.STUDIO

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = isChromeVisible,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
            ) {
                Column {
                    MiniPlayer(
                        uiState = playerUiState,
                        positionFlow = positionFlow,
                        onPlayPause = onPlayPause,
                        onClick = onOpenPlayer
                    )
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
