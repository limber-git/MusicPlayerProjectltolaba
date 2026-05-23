package com.limbe.hexamusicplayer.infrastructure.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.limbe.hexamusicplayer.R
import com.limbe.hexamusicplayer.app.MusicApplication
import com.limbe.hexamusicplayer.ui.MainActivity

class PlaybackMediaSessionService : MediaSessionService() {

    private val appContainer by lazy { (application as MusicApplication).container }
    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private var isInForeground = false
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            refreshNotification()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            refreshNotification()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            refreshNotification()
        }
    }

    override fun onCreate() {
        super.onCreate()
        appContainer.playbackSessionManager.getOrCreateSession()
        appContainer.exoPlayer.addListener(playerListener)
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            appContainer.exoPlayer.pause()
            stopForegroundCompat()
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundIfNeeded()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return appContainer.playbackSessionManager.getOrCreateSession()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!appContainer.exoPlayer.isPlaying) {
            stopForegroundCompat()
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        appContainer.exoPlayer.removeListener(playerListener)
        stopForegroundCompat()
        appContainer.playbackSessionManager.release()
        super.onDestroy()
    }

    private fun startForegroundIfNeeded() {
        val notification = buildNotification()
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )
        isInForeground = true
    }

    private fun stopForegroundCompat() {
        if (!isInForeground) return
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        isInForeground = false
    }

    private fun refreshNotification() {
        if (!isInForeground) return
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_playback_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_playback_description)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val metadata = appContainer.exoPlayer.currentMediaItem?.mediaMetadata
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            this,
            REQUEST_CODE_OPEN_APP,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = Intent(this, PlaybackMediaSessionService::class.java).apply {
            action = ACTION_STOP
            component = ComponentName(this@PlaybackMediaSessionService, PlaybackMediaSessionService::class.java)
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            REQUEST_CODE_STOP_SERVICE,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = metadata?.title?.toString().orEmpty().ifBlank {
            getString(R.string.notification_playback_title_fallback)
        }
        val text = metadata?.artist?.toString().orEmpty().ifBlank {
            getString(R.string.notification_playback_text_fallback)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_icon)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(contentIntent)
            .setDeleteIntent(stopPendingIntent)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setOngoing(appContainer.exoPlayer.isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .build()
    }

    companion object {
        private const val ACTION_START = "com.limbe.hexamusicplayer.action.SESSION_START"
        private const val ACTION_STOP = "com.limbe.hexamusicplayer.action.SESSION_STOP"
        private const val CHANNEL_ID = "hexa_playback"
        private const val NOTIFICATION_ID = 4101
        private const val REQUEST_CODE_OPEN_APP = 6101
        private const val REQUEST_CODE_STOP_SERVICE = 6102

        fun start(context: Context) {
            val intent = Intent(context, PlaybackMediaSessionService::class.java).apply {
                action = ACTION_START
            }
            runCatching {
                ContextCompat.startForegroundService(context, intent)
            }.onFailure {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, PlaybackMediaSessionService::class.java).apply {
                action = ACTION_STOP
            }
            runCatching {
                context.startService(intent)
            }
        }
    }
}
