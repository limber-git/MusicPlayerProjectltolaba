package com.limbe.hexamusicplayer.infrastructure.session

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.limbe.hexamusicplayer.ui.MainActivity

class PlaybackSessionManager(
    private val context: Context,
    private val exoPlayer: ExoPlayer
) {

    @Volatile
    private var mediaSession: MediaSession? = null

    fun getOrCreateSession(): MediaSession {
        mediaSession?.let { return it }

        synchronized(this) {
            mediaSession?.let { return it }

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val session = MediaSession.Builder(context, exoPlayer)
                .setId(SESSION_ID)
                .setSessionActivity(
                    PendingIntent.getActivity(
                        context,
                        REQUEST_CODE_OPEN_APP,
                        launchIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .build()

            mediaSession = session
            return session
        }
    }

    fun release() {
        synchronized(this) {
            mediaSession?.release()
            mediaSession = null
        }
    }

    companion object {
        private const val REQUEST_CODE_OPEN_APP = 6101
        private const val SESSION_ID = "hexa-music-main-session"
    }
}
