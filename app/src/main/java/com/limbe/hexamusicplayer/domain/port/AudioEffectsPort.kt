package com.limbe.hexamusicplayer.domain.port

import com.limbe.hexamusicplayer.domain.model.AudioEffectsState
import kotlinx.coroutines.flow.StateFlow

interface AudioEffectsPort {
    val state: StateFlow<AudioEffectsState>

    fun attachToSession(sessionId: Int)
    fun setBandLevel(index: Int, level: Int)
    fun setBassStrength(strength: Int)
    fun setVirtualizerStrength(strength: Int)
    fun setLoudnessGainMb(gainMb: Int)
    fun release()
}
