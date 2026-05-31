package com.limbe.hexamusicplayer.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.limbe.hexamusicplayer.R
import com.limbe.hexamusicplayer.ui.navigation.AppRoute

@Composable
fun HexaBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == AppRoute.HOME,
            onClick = { onNavigate(AppRoute.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_home)) },
            colors = navItemColors
        )
        NavigationBarItem(
            selected = currentRoute == AppRoute.EXPLORER,
            onClick = { onNavigate(AppRoute.EXPLORER) },
            icon = { Icon(Icons.Default.Folder, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_explore)) },
            colors = navItemColors
        )
        NavigationBarItem(
            selected = currentRoute == AppRoute.LIBRARY,
            onClick = { onNavigate(AppRoute.LIBRARY) },
            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_library)) },
            colors = navItemColors
        )
        NavigationBarItem(
            selected = currentRoute == AppRoute.SEARCH,
            onClick = { onNavigate(AppRoute.SEARCH) },
            icon = { Icon(Icons.Default.ManageSearch, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_search)) },
            colors = navItemColors
        )
    }
}
