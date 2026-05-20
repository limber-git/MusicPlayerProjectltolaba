package com.limbe.hexamusicplayer.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.limbe.hexamusicplayer.BuildConfig
import com.limbe.hexamusicplayer.R
import com.limbe.hexamusicplayer.domain.model.AppLanguage
import com.limbe.hexamusicplayer.domain.model.DarkModeMode
import com.limbe.hexamusicplayer.ui.screens.player.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    val uiState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        treeUri ?: return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
        playerViewModel.setManualLibraryFolder(
            uri = treeUri.toString(),
            label = formatFolderLabel(treeUri)
        )
    }

    if (showThemeDialog) {
        ThemeModeDialog(
            selectedMode = uiState.darkModeMode,
            onDismiss = { showThemeDialog = false },
            onModeSelected = { mode ->
                playerViewModel.setDarkModeMode(mode)
                showThemeDialog = false
            }
        )
    }

    if (showLanguageDialog) {
        LanguageDialog(
            selectedLanguage = uiState.appLanguage,
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { language ->
                playerViewModel.setAppLanguage(language)
                showLanguageDialog = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { SettingsSectionTitle(stringResource(R.string.settings_section_appearance)) }
            item {
                SettingsClickableItem(
                    title = stringResource(R.string.settings_theme_title),
                    subtitle = themeModeLabel(uiState.darkModeMode),
                    icon = Icons.Default.Palette,
                    onClick = { showThemeDialog = true }
                )
            }
            item {
                SettingsClickableItem(
                    title = stringResource(R.string.settings_language_title),
                    subtitle = appLanguageLabel(uiState.appLanguage),
                    icon = Icons.Default.Language,
                    onClick = { showLanguageDialog = true }
                )
            }

            item { SettingsSectionTitle(stringResource(R.string.settings_section_audio)) }
            item {
                SettingsToggleItem(
                    title = stringResource(R.string.settings_safe_audio_title),
                    subtitle = stringResource(
                        if (uiState.audioEffectsEnabled) {
                            R.string.settings_safe_audio_subtitle_enabled
                        } else {
                            R.string.settings_safe_audio_subtitle_disabled
                        }
                    ),
                    icon = Icons.Default.Security,
                    checked = uiState.audioEffectsEnabled,
                    onCheckedChange = playerViewModel::setAudioEffectsEnabled
                )
            }
            item {
                SettingsInfoItem(
                    title = stringResource(R.string.settings_audio_engine_title),
                    subtitle = stringResource(
                        if (uiState.effectsAvailable && uiState.audioEffectsEnabled) {
                            R.string.settings_audio_engine_status_active
                        } else if (!uiState.audioEffectsEnabled) {
                            R.string.settings_audio_engine_status_safe
                        } else {
                            R.string.settings_audio_engine_status_limited
                        }
                    ),
                    icon = Icons.Default.Style
                )
            }

            item { SettingsSectionTitle(stringResource(R.string.settings_section_library)) }
            item {
                SettingsInfoItem(
                    title = stringResource(R.string.settings_music_source_title),
                    subtitle = uiState.manualLibraryFolderLabel?.let {
                        stringResource(R.string.settings_music_source_subtitle_folder, it)
                    } ?: stringResource(R.string.settings_music_source_subtitle_device),
                    icon = Icons.Default.FolderOpen
                )
            }
            item {
                SettingsClickableItem(
                    title = stringResource(R.string.settings_select_folder_title),
                    subtitle = stringResource(R.string.settings_select_folder_subtitle),
                    icon = Icons.Default.FolderOpen,
                    onClick = { folderPickerLauncher.launch(null) }
                )
            }
            if (uiState.manualLibraryFolderUri != null) {
                item {
                    SettingsClickableItem(
                        title = stringResource(R.string.settings_clear_folder_title),
                        subtitle = stringResource(R.string.settings_clear_folder_subtitle),
                        icon = Icons.Default.FolderOpen,
                        onClick = {
                            uiState.manualLibraryFolderUri?.let { rawUri ->
                                runCatching {
                                    context.contentResolver.releasePersistableUriPermission(
                                        Uri.parse(rawUri),
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                    )
                                }
                            }
                            playerViewModel.setManualLibraryFolder(null, null)
                        }
                    )
                }
            }
            item {
                SettingsInfoItem(
                    title = stringResource(R.string.settings_favorites_title),
                    subtitle = stringResource(R.string.settings_favorites_subtitle, uiState.favoriteCount),
                    icon = Icons.Default.Favorite
                )
            }
            item {
                SettingsInfoItem(
                    title = stringResource(R.string.settings_recent_title),
                    subtitle = stringResource(R.string.settings_recent_subtitle, uiState.recentTrackIds.size),
                    icon = Icons.Default.History
                )
            }

            item { SettingsSectionTitle(stringResource(R.string.settings_section_about)) }
            item {
                SettingsInfoItem(
                    title = stringResource(R.string.settings_version_title),
                    subtitle = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    icon = Icons.Default.Info
                )
            }
        }
    }
}

@Composable
private fun ThemeModeDialog(
    selectedMode: DarkModeMode,
    onDismiss: () -> Unit,
    onModeSelected: (DarkModeMode) -> Unit
) {
    val options = listOf(
        DarkModeMode.SYSTEM to stringResource(R.string.theme_mode_system),
        DarkModeMode.LIGHT to stringResource(R.string.theme_mode_light),
        DarkModeMode.DARK to stringResource(R.string.theme_mode_dark)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_theme_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { (mode, label) ->
                    Surface(
                        onClick = { onModeSelected(mode) },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMode == mode,
                                onClick = { onModeSelected(mode) }
                            )
                            Text(text = label)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_done))
            }
        }
    )
}

@Composable
private fun LanguageDialog(
    selectedLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    val options = listOf(
        AppLanguage.SYSTEM to stringResource(R.string.language_mode_system),
        AppLanguage.SPANISH to stringResource(R.string.language_mode_spanish),
        AppLanguage.ENGLISH to stringResource(R.string.language_mode_english)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { (language, label) ->
                    Surface(
                        onClick = { onLanguageSelected(language) },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == language,
                                onClick = { onLanguageSelected(language) }
                            )
                            Text(text = label)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_done))
            }
        }
    )
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
private fun SettingsClickableItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        SettingsItemContent(title = title, subtitle = subtitle, icon = icon, trailing = null)
    }
}

@Composable
private fun SettingsInfoItem(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    SettingsItemContent(title = title, subtitle = subtitle, icon = icon, trailing = null)
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsItemContent(
        title = title,
        subtitle = subtitle,
        icon = icon,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

@Composable
private fun SettingsItemContent(
    title: String,
    subtitle: String,
    icon: ImageVector,
    trailing: (@Composable () -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        trailing?.invoke()
    }
}

@Composable
private fun themeModeLabel(mode: DarkModeMode): String {
    return when (mode) {
        DarkModeMode.SYSTEM -> stringResource(R.string.theme_mode_system)
        DarkModeMode.LIGHT -> stringResource(R.string.theme_mode_light)
        DarkModeMode.DARK -> stringResource(R.string.theme_mode_dark)
    }
}

@Composable
private fun appLanguageLabel(language: AppLanguage): String {
    return when (language) {
        AppLanguage.SYSTEM -> stringResource(R.string.language_mode_system)
        AppLanguage.SPANISH -> stringResource(R.string.language_mode_spanish)
        AppLanguage.ENGLISH -> stringResource(R.string.language_mode_english)
    }
}

private fun formatFolderLabel(uri: Uri): String {
    val rawValue = uri.lastPathSegment.orEmpty()
    return rawValue.substringAfterLast(":").ifBlank { rawValue }.replace('/', ' ')
}
