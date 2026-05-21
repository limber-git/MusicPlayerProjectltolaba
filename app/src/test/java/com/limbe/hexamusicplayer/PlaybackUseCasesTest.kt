package com.limbe.hexamusicplayer

import com.limbe.hexamusicplayer.domain.model.PlayerState
import com.limbe.hexamusicplayer.domain.model.RepeatMode
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.port.AudioPlayerPort
import com.limbe.hexamusicplayer.domain.usecase.SeekToUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetPlaybackPitchUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetPlaybackSpeedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackUseCasesTest {

    @Test
    fun `set speed clamps minimum and maximum`() {
        val fakePlayer = FakeUseCasePlayerPort()
        val useCase = SetPlaybackSpeedUseCase(fakePlayer)

        useCase(0.1f)
        assertEquals(0.5f, fakePlayer.lastSpeed, 0.0f)

        useCase(2.7f)
        assertEquals(2.0f, fakePlayer.lastSpeed, 0.0f)
    }

    @Test
    fun `set pitch clamps minimum and maximum`() {
        val fakePlayer = FakeUseCasePlayerPort()
        val useCase = SetPlaybackPitchUseCase(fakePlayer)

        useCase(0.2f)
        assertEquals(0.5f, fakePlayer.lastPitch, 0.0f)

        useCase(3.5f)
        assertEquals(2.0f, fakePlayer.lastPitch, 0.0f)
    }

    @Test
    fun `seek clamps negative values to zero`() {
        val fakePlayer = FakeUseCasePlayerPort()
        val useCase = SeekToUseCase(fakePlayer)

        useCase(-2500L)
        assertEquals(0L, fakePlayer.lastSeekMs)

        useCase(9876L)
        assertEquals(9876L, fakePlayer.lastSeekMs)
    }
}

private class FakeUseCasePlayerPort : AudioPlayerPort {
    private val stateFlow = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = stateFlow

    var lastSpeed: Float = 1.0f
    var lastPitch: Float = 1.0f
    var lastSeekMs: Long = 0L
    var shuffleEnabled: Boolean = false
    var repeatModeValue: RepeatMode = RepeatMode.OFF

    override fun play(track: Track, queue: List<Track>) = Unit

    override fun togglePlayPause() = Unit

    override fun seekTo(positionMs: Long) {
        lastSeekMs = positionMs
    }

    override fun setSpeed(speed: Float) {
        lastSpeed = speed
    }

    override fun setPitch(pitch: Float) {
        lastPitch = pitch
    }

    override fun skipToNext() = Unit

    override fun skipToPrevious() = Unit

    override fun setShuffleMode(enabled: Boolean) {
        shuffleEnabled = enabled
    }

    override fun setRepeatMode(mode: RepeatMode) {
        repeatModeValue = mode
    }

    override fun addToQueue(track: Track) {
        stateFlow.value = stateFlow.value.copy(queue = stateFlow.value.queue + track)
    }

    override fun playNext(track: Track) {
        val queue = stateFlow.value.queue
        if (queue.isEmpty()) {
            stateFlow.value = stateFlow.value.copy(currentTrack = track, queue = listOf(track), isPlaying = true)
            return
        }

        val currentIndex = queue.indexOfFirst { it.id == stateFlow.value.currentTrack?.id }.coerceAtLeast(0)
        val updatedQueue = queue.toMutableList().apply {
            add((currentIndex + 1).coerceAtMost(size), track)
        }
        stateFlow.value = stateFlow.value.copy(queue = updatedQueue)
    }

    override fun removeFromQueue(trackId: Long) {
        stateFlow.value = stateFlow.value.copy(queue = stateFlow.value.queue.filterNot { it.id == trackId })
    }

    override fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val queue = stateFlow.value.queue.toMutableList()
        if (fromIndex !in queue.indices || toIndex !in queue.indices) return
        val track = queue.removeAt(fromIndex)
        queue.add(toIndex, track)
        stateFlow.value = stateFlow.value.copy(queue = queue)
    }

    override fun release() = Unit
}
