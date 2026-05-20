package com.limbe.hexamusicplayer.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limbe.hexamusicplayer.app.MusicApplication
import com.limbe.hexamusicplayer.ui.screens.library.LibraryViewModel
import com.limbe.hexamusicplayer.ui.screens.player.PlayerViewModel
import com.limbe.hexamusicplayer.ui.theme.HexaMusicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as MusicApplication).container

        setContent {
            val factory = MusicPlayerViewModelFactory(container)
            val libraryViewModel: LibraryViewModel = viewModel(factory = factory)
            val playerViewModel: PlayerViewModel = viewModel(factory = factory)
            val playerUiState by playerViewModel.uiState.collectAsStateWithLifecycle()

            HexaMusicTheme(darkModeMode = playerUiState.darkModeMode) {
                HexaApp(
                    libraryViewModel = libraryViewModel,
                    playerViewModel = playerViewModel
                )
            }
        }
    }
}
