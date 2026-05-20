package com.limbe.hexamusicplayer.ui.util

import android.Manifest
import android.content.Context
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun requiredAudioPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

fun hasAudioPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        requiredAudioPermission()
    ) == PackageManager.PERMISSION_GRANTED
}
