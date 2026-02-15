package com.limbe.hexamusicplayer.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limbe.hexamusicplayer.app.MusicApplication
import com.limbe.hexamusicplayer.ui.theme.HexaMusicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as MusicApplication).container

        setContent {
            HexaMusicTheme {
                val viewModel: MusicPlayerViewModel = viewModel(
                    factory = MusicPlayerViewModelFactory(container)
                )
                MusicPlayerScreen(viewModel = viewModel)
            }
        }
    }
}
