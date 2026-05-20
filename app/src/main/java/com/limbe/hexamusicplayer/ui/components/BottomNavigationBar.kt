package com.limbe.hexamusicplayer.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.limbe.hexamusicplayer.R

@Composable
fun HexaBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "library",
            onClick = { onNavigate("library") },
            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_library)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "explorer",
            onClick = { onNavigate("explorer") },
            icon = { Icon(Icons.Default.Folder, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_albums)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "studio",
            onClick = { onNavigate("studio") },
            icon = { Icon(Icons.Default.GraphicEq, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_studio)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "player",
            onClick = { onNavigate("player") },
            icon = { Icon(Icons.Default.MusicNote, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_now_playing)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )
    }
}
