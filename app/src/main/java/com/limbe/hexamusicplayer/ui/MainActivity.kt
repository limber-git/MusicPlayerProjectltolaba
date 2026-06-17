package com.limbe.hexamusicplayer.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limbe.hexamusicplayer.app.MusicApplication
import com.limbe.hexamusicplayer.domain.model.AppLanguage
import com.limbe.hexamusicplayer.domain.model.DarkModeMode
import com.limbe.hexamusicplayer.ui.screens.library.LibraryViewModel
import com.limbe.hexamusicplayer.ui.screens.player.PlayerViewModel
import com.limbe.hexamusicplayer.ui.theme.HexaMusicTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as MusicApplication).container

        setContent {
            val factory = MusicPlayerViewModelFactory(container)
            val libraryViewModel: LibraryViewModel = viewModel(factory = factory)
            val playerViewModel: PlayerViewModel = viewModel(factory = factory)
            val appShellFlow = remember(playerViewModel) {
                playerViewModel.uiState
                    .map { it.darkModeMode to it.appLanguage }
                    .distinctUntilChanged()
            }
            val appShellState by appShellFlow.collectAsStateWithLifecycle(
                DarkModeMode.SYSTEM to AppLanguage.SYSTEM
            )

            LaunchedEffect(appShellState.second) {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(appShellState.second.languageTag)
                )
            }

            HexaMusicTheme(darkModeMode = appShellState.first) {
                HexaApp(
                    libraryViewModel = libraryViewModel,
                    playerViewModel = playerViewModel
                )
            }
        }
    }
}
