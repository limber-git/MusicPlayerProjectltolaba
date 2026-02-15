package com.limbe.hexamusicplayer.infrastructure.effects

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import com.limbe.hexamusicplayer.domain.model.AudioEffectsState
import com.limbe.hexamusicplayer.domain.model.EqBand
import com.limbe.hexamusicplayer.domain.port.AudioEffectsPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidAudioEffectsAdapter : AudioEffectsPort {

    private val _state = MutableStateFlow(AudioEffectsState())
    override val state: StateFlow<AudioEffectsState> = _state.asStateFlow()

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var currentLoudnessGainMb: Int = 0

    override fun attachToSession(sessionId: Int) {
        if (sessionId <= 0 || sessionId == _state.value.attachedSessionId) {
            return
        }

        releaseEffectsOnly()

        runCatching {
            equalizer = Equalizer(0, sessionId).apply { enabled = true }
            bassBoost = BassBoost(0, sessionId).apply { enabled = true }
            virtualizer = Virtualizer(0, sessionId).apply { enabled = true }
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply { enabled = true }
        }

        currentLoudnessGainMb = 0
        publishState(sessionId)
    }

    override fun setBandLevel(index: Int, level: Int) {
        val eq = equalizer ?: return
        val bandIndex = index.toShort()
        if (bandIndex < 0 || bandIndex >= eq.numberOfBands) {
            return
        }

        val range = eq.bandLevelRange
        val clamped = level.coerceIn(range[0].toInt(), range[1].toInt())
        eq.setBandLevel(bandIndex, clamped.toShort())
        publishState(_state.value.attachedSessionId)
    }

    override fun setBassStrength(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        bassBoost?.setStrength(clamped.toShort())
        publishState(_state.value.attachedSessionId)
    }

    override fun setVirtualizerStrength(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        virtualizer?.setStrength(clamped.toShort())
        publishState(_state.value.attachedSessionId)
    }

    override fun setLoudnessGainMb(gainMb: Int) {
        val clamped = gainMb.coerceIn(-1500, 3000)
        loudnessEnhancer?.setTargetGain(clamped)
        currentLoudnessGainMb = clamped
        publishState(_state.value.attachedSessionId)
    }

    override fun release() {
        releaseEffectsOnly()
        _state.value = AudioEffectsState()
        currentLoudnessGainMb = 0
    }

    private fun publishState(sessionId: Int?) {
        val eq = equalizer
        val eqBands = if (eq == null) {
            emptyList()
        } else {
            val range = eq.bandLevelRange
            val minLevel = range[0].toInt()
            val maxLevel = range[1].toInt()
            (0 until eq.numberOfBands.toInt()).map { band ->
                val bandShort = band.toShort()
                EqBand(
                    index = band,
                    centerFreqHz = (eq.getCenterFreq(bandShort) / 1000).toInt(),
                    level = eq.getBandLevel(bandShort).toInt(),
                    minLevel = minLevel,
                    maxLevel = maxLevel
                )
            }
        }

        _state.value = AudioEffectsState(
            attachedSessionId = sessionId,
            bands = eqBands,
            bassStrength = bassBoost?.roundedStrength?.toInt() ?: 0,
            virtualizerStrength = virtualizer?.roundedStrength?.toInt() ?: 0,
            loudnessGainMb = currentLoudnessGainMb
        )
    }

    private fun releaseEffectsOnly() {
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }
        runCatching { virtualizer?.release() }
        runCatching { loudnessEnhancer?.release() }

        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
    }
}
