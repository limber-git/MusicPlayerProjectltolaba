package com.limbe.hexamusicplayer.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.limbe.hexamusicplayer.R
import com.limbe.hexamusicplayer.infrastructure.service.PlaybackMediaSessionService
import com.limbe.hexamusicplayer.ui.MainActivity

class StudioMusicWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, buildRemoteViews(context))
        }
    }

    private fun buildRemoteViews(context: Context): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_studio_music).apply {
            setOnClickPendingIntent(R.id.widget_root, buildOpenAppPendingIntent(context))
            setOnClickPendingIntent(
                R.id.widget_previous_button,
                buildServiceActionPendingIntent(context, ACTION_PREVIOUS, REQUEST_CODE_PREVIOUS)
            )
            setOnClickPendingIntent(
                R.id.widget_play_pause_button,
                buildServiceActionPendingIntent(context, ACTION_PLAY_PAUSE, REQUEST_CODE_PLAY_PAUSE)
            )
            setOnClickPendingIntent(
                R.id.widget_next_button,
                buildServiceActionPendingIntent(context, ACTION_NEXT, REQUEST_CODE_NEXT)
            )
        }
    }

    private fun buildOpenAppPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_APP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildServiceActionPendingIntent(
        context: Context,
        action: String,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, PlaybackMediaSessionService::class.java).apply {
            this.action = action
            component = ComponentName(context, PlaybackMediaSessionService::class.java)
        }
        return PendingIntent.getForegroundService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val ACTION_PREVIOUS = "com.tolaba.studiomusic.action.SESSION_PREVIOUS"
        private const val ACTION_PLAY_PAUSE = "com.tolaba.studiomusic.action.SESSION_PLAY_PAUSE"
        private const val ACTION_NEXT = "com.tolaba.studiomusic.action.SESSION_NEXT"

        private const val REQUEST_CODE_OPEN_APP = 7101
        private const val REQUEST_CODE_PREVIOUS = 7102
        private const val REQUEST_CODE_PLAY_PAUSE = 7103
        private const val REQUEST_CODE_NEXT = 7104
    }
}
