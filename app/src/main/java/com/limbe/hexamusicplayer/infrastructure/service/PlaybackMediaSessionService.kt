package com.limbe.hexamusicplayer.infrastructure.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.limbe.hexamusicplayer.app.MusicApplication

class PlaybackMediaSessionService : MediaSessionService() {

    private val appContainer by lazy { (application as MusicApplication).container }

    override fun onCreate() {
        super.onCreate()
        appContainer.playbackSessionManager.getOrCreateSession()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            appContainer.exoPlayer.pause()
            stopSelf()
            return START_NOT_STICKY
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return appContainer.playbackSessionManager.getOrCreateSession()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!appContainer.exoPlayer.isPlaying) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        appContainer.playbackSessionManager.release()
        super.onDestroy()
    }

    companion object {
        private const val ACTION_START = "com.limbe.hexamusicplayer.action.SESSION_START"
        private const val ACTION_STOP = "com.limbe.hexamusicplayer.action.SESSION_STOP"

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
