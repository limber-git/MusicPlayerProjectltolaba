package com.limbe.hexamusicplayer.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun HexaBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF0B1428),
        contentColor = Color.White
    ) {
        NavigationBarItem(
            selected = currentRoute == "library",
            onClick = { onNavigate("library") },
            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) },
            label = { Text("Música") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF5CF2D7),
                selectedTextColor = Color(0xFF5CF2D7),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF15233F)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "explorer",
            onClick = { onNavigate("explorer") },
            icon = { Icon(Icons.Default.Folder, contentDescription = null) },
            label = { Text("Carpetas") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF74F9D9),
                selectedTextColor = Color(0xFF74F9D9),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF15233F)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "studio",
            onClick = { onNavigate("studio") },
            icon = { Icon(Icons.Default.GraphicEq, contentDescription = null) },
            label = { Text("Studio") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFF9B6A),
                selectedTextColor = Color(0xFFFF9B6A),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF15233F)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "player",
            onClick = { onNavigate("player") },
            icon = { Icon(Icons.Default.MusicNote, contentDescription = null) },
            label = { Text("Sonando") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFF7D70),
                selectedTextColor = Color(0xFFFF7D70),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF15233F)
            )
        )
    }
}
