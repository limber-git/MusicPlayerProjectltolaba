package com.limbe.hexamusicplayer.domain.usecase

import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.port.AudioPlayerPort

class AddToQueueUseCase(
    private val audioPlayerPort: AudioPlayerPort
) {
    operator fun invoke(track: Track) = audioPlayerPort.addToQueue(track)
}

class PlayNextUseCase(
    private val audioPlayerPort: AudioPlayerPort
) {
    operator fun invoke(track: Track) = audioPlayerPort.playNext(track)
}

class RemoveFromQueueUseCase(
    private val audioPlayerPort: AudioPlayerPort
) {
    operator fun invoke(trackId: Long) = audioPlayerPort.removeFromQueue(trackId)
}

class MoveQueueItemUseCase(
    private val audioPlayerPort: AudioPlayerPort
) {
    operator fun invoke(fromIndex: Int, toIndex: Int) = audioPlayerPort.moveQueueItem(fromIndex, toIndex)
}
