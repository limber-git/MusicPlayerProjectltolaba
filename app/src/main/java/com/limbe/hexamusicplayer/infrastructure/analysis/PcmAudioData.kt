package com.limbe.hexamusicplayer.infrastructure.analysis

data class PcmAudioData(
    val samples: FloatArray,
    val sampleRate: Int,
    val durationMs: Long
)
