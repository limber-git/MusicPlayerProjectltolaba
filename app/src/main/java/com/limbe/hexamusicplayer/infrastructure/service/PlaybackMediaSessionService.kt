package com.limbe.hexamusicplayer.infrastructure.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
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
    private var lastNotificationSnapshot: NotificationSnapshot? = null
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
        when (intent?.action) {
            ACTION_PREVIOUS -> {
                appContainer.exoPlayer.seekToPreviousMediaItem()
            }
            ACTION_PLAY_PAUSE -> {
                if (appContainer.exoPlayer.isPlaying) {
                    appContainer.exoPlayer.pause()
                } else {
                    appContainer.exoPlayer.play()
                }
            }
            ACTION_NEXT -> {
                appContainer.exoPlayer.seekToNextMediaItem()
            }
            ACTION_STOP -> {
                appContainer.exoPlayer.pause()
                stopForegroundCompat()
                stopSelf()
                return START_NOT_STICKY
            }
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
        if (isInForeground) {
            refreshNotification()
            return
        }
        val notification = buildNotification()
        val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0
        }
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            foregroundServiceType
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
        val snapshot = currentNotificationSnapshot()
        if (snapshot == lastNotificationSnapshot) return
        lastNotificationSnapshot = snapshot
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun ensureNotificationChannel() {
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
        val previousPendingIntent = PendingIntent.getService(
            this,
            REQUEST_CODE_PREVIOUS,
            buildServiceIntent(ACTION_PREVIOUS),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val playPausePendingIntent = PendingIntent.getService(
            this,
            REQUEST_CODE_PLAY_PAUSE,
            buildServiceIntent(ACTION_PLAY_PAUSE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nextPendingIntent = PendingIntent.getService(
            this,
            REQUEST_CODE_NEXT,
            buildServiceIntent(ACTION_NEXT),
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
            .addAction(
                android.R.drawable.ic_media_previous,
                getString(R.string.action_previous),
                previousPendingIntent
            )
            .addAction(
                if (appContainer.exoPlayer.isPlaying) {
                    android.R.drawable.ic_media_pause
                } else {
                    android.R.drawable.ic_media_play
                },
                getString(R.string.action_play_pause),
                playPausePendingIntent
            )
            .addAction(
                android.R.drawable.ic_media_next,
                getString(R.string.action_next),
                nextPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.notification_action_stop),
                stopPendingIntent
            )
            .build()
    }

    private fun currentNotificationSnapshot(): NotificationSnapshot {
        val metadata = appContainer.exoPlayer.currentMediaItem?.mediaMetadata
        return NotificationSnapshot(
            title = metadata?.title?.toString().orEmpty(),
            artist = metadata?.artist?.toString().orEmpty(),
            isPlaying = appContainer.exoPlayer.isPlaying
        )
    }

    private fun buildServiceIntent(action: String): Intent {
        return Intent(this, PlaybackMediaSessionService::class.java).apply {
            this.action = action
            component = ComponentName(this@PlaybackMediaSessionService, PlaybackMediaSessionService::class.java)
        }
    }

    companion object {
        private const val ACTION_START = "com.tolaba.studiomusic.action.SESSION_START"
        private const val ACTION_PREVIOUS = "com.tolaba.studiomusic.action.SESSION_PREVIOUS"
        private const val ACTION_PLAY_PAUSE = "com.tolaba.studiomusic.action.SESSION_PLAY_PAUSE"
        private const val ACTION_NEXT = "com.tolaba.studiomusic.action.SESSION_NEXT"
        private const val ACTION_STOP = "com.tolaba.studiomusic.action.SESSION_STOP"
        private const val CHANNEL_ID = "studio_music_playback"
        private const val NOTIFICATION_ID = 4101
        private const val REQUEST_CODE_OPEN_APP = 6101
        private const val REQUEST_CODE_PREVIOUS = 6102
        private const val REQUEST_CODE_PLAY_PAUSE = 6103
        private const val REQUEST_CODE_NEXT = 6104
        private const val REQUEST_CODE_STOP_SERVICE = 6105

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

    private data class NotificationSnapshot(
        val title: String,
        val artist: String,
        val isPlaying: Boolean
    )
}
