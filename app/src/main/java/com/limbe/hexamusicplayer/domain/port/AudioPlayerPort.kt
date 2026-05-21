package com.limbe.hexamusicplayer.domain.port

import com.limbe.hexamusicplayer.domain.model.PlayerState
import com.limbe.hexamusicplayer.domain.model.Track
import kotlinx.coroutines.flow.StateFlow

interface AudioPlayerPort {
    val state: StateFlow<PlayerState>

    fun play(track: Track, queue: List<Track> = emptyList())
    fun togglePlayPause()
    fun seekTo(positionMs: Long)
    fun setSpeed(speed: Float)
    fun setPitch(pitch: Float)
    fun skipToNext()
    fun skipToPrevious()
    fun setShuffleMode(enabled: Boolean)
    fun setRepeatMode(mode: com.limbe.hexamusicplayer.domain.model.RepeatMode)
    fun addToQueue(track: Track)
    fun playNext(track: Track)
    fun removeFromQueue(trackId: Long)
    fun moveQueueItem(fromIndex: Int, toIndex: Int)
    fun release()
}
