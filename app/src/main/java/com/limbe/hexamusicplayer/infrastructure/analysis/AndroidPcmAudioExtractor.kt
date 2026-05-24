package com.limbe.hexamusicplayer.infrastructure.analysis

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import com.limbe.hexamusicplayer.domain.model.Track
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AndroidPcmAudioExtractor(
    private val context: Context
) {

    fun extract(track: Track, maxDurationMs: Long = DEFAULT_MAX_DURATION_MS): PcmAudioData {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null

        try {
            extractor.setDataSource(context, Uri.parse(track.contentUri), null)
            val trackIndex = findAudioTrack(extractor)
            if (trackIndex < 0) {
                error("No audio track found")
            }

            extractor.selectTrack(trackIndex)
            val inputFormat = extractor.getTrackFormat(trackIndex)
            val mime = inputFormat.getString(MediaFormat.KEY_MIME) ?: error("Missing audio mime type")
            val decoder = MediaCodec.createDecoderByType(mime)
            codec = decoder
            decoder.configure(inputFormat, null, null, 0)
            decoder.start()

            return decodeSelectedTrack(
                extractor = extractor,
                codec = decoder,
                maxDurationMs = maxDurationMs
            )
        } finally {
            runCatching { codec?.stop() }
            runCatching { codec?.release() }
            runCatching { extractor.release() }
        }
    }

    private fun findAudioTrack(extractor: MediaExtractor): Int {
        for (index in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(index)
            val mime = format.getString(MediaFormat.KEY_MIME).orEmpty()
            if (mime.startsWith("audio/")) return index
        }
        return -1
    }

    private fun decodeSelectedTrack(
        extractor: MediaExtractor,
        codec: MediaCodec,
        maxDurationMs: Long
    ): PcmAudioData {
        val info = MediaCodec.BufferInfo()
        val monoSamples = ArrayList<Float>()
        var sawInputEnd = false
        var sawOutputEnd = false
        var outputFormat = codec.outputFormat
        var sampleRate = outputFormat.getIntegerOrDefault(MediaFormat.KEY_SAMPLE_RATE, 44_100)
        var channelCount = outputFormat.getIntegerOrDefault(MediaFormat.KEY_CHANNEL_COUNT, 2)
        var pcmEncoding = outputFormat.getIntegerOrDefault(
            MediaFormat.KEY_PCM_ENCODING,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val maxSamples = ((maxDurationMs / 1000.0) * sampleRate).toInt().coerceAtLeast(sampleRate)

        while (!sawOutputEnd && monoSamples.size < maxSamples) {
            if (!sawInputEnd) {
                val inputIndex = codec.dequeueInputBuffer(TIMEOUT_US)
                if (inputIndex >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputIndex)
                    val sampleSize = inputBuffer?.let { extractor.readSampleData(it, 0) } ?: -1
                    if (sampleSize < 0) {
                        codec.queueInputBuffer(
                            inputIndex,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        sawInputEnd = true
                    } else {
                        codec.queueInputBuffer(
                            inputIndex,
                            0,
                            sampleSize,
                            extractor.sampleTime.coerceAtLeast(0L),
                            0
                        )
                        extractor.advance()
                    }
                }
            }

            when (val outputIndex = codec.dequeueOutputBuffer(info, TIMEOUT_US)) {
                MediaCodec.INFO_TRY_AGAIN_LATER -> Unit
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    outputFormat = codec.outputFormat
                    sampleRate = outputFormat.getIntegerOrDefault(MediaFormat.KEY_SAMPLE_RATE, sampleRate)
                    channelCount = outputFormat.getIntegerOrDefault(MediaFormat.KEY_CHANNEL_COUNT, channelCount)
                    pcmEncoding = outputFormat.getIntegerOrDefault(MediaFormat.KEY_PCM_ENCODING, pcmEncoding)
                }
                else -> if (outputIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputIndex)
                    if (outputBuffer != null && info.size > 0) {
                        appendMonoSamples(
                            buffer = outputBuffer,
                            info = info,
                            channelCount = channelCount.coerceAtLeast(1),
                            pcmEncoding = pcmEncoding,
                            output = monoSamples,
                            maxSamples = maxSamples
                        )
                    }

                    if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        sawOutputEnd = true
                    }
                    codec.releaseOutputBuffer(outputIndex, false)
                }
            }
        }

        val durationMs = ((monoSamples.size.toDouble() / sampleRate) * 1000.0).toLong()
        return PcmAudioData(
            samples = monoSamples.toFloatArray(),
            sampleRate = sampleRate,
            durationMs = durationMs
        )
    }

    private fun appendMonoSamples(
        buffer: ByteBuffer,
        info: MediaCodec.BufferInfo,
        channelCount: Int,
        pcmEncoding: Int,
        output: MutableList<Float>,
        maxSamples: Int
    ) {
        val duplicate = buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN)
        duplicate.position(info.offset)
        duplicate.limit(info.offset + info.size)

        when (pcmEncoding) {
            AudioFormat.ENCODING_PCM_FLOAT -> appendFloatSamples(duplicate, channelCount, output, maxSamples)
            else -> appendShortSamples(duplicate, channelCount, output, maxSamples)
        }
    }

    private fun appendShortSamples(
        buffer: ByteBuffer,
        channelCount: Int,
        output: MutableList<Float>,
        maxSamples: Int
    ) {
        while (buffer.remaining() >= SHORT_BYTES * channelCount && output.size < maxSamples) {
            var sum = 0f
            repeat(channelCount) {
                sum += buffer.short / SHORT_SCALE
            }
            output += sum / channelCount
        }
    }

    private fun appendFloatSamples(
        buffer: ByteBuffer,
        channelCount: Int,
        output: MutableList<Float>,
        maxSamples: Int
    ) {
        while (buffer.remaining() >= FLOAT_BYTES * channelCount && output.size < maxSamples) {
            var sum = 0f
            repeat(channelCount) {
                sum += buffer.float.coerceIn(-1f, 1f)
            }
            output += sum / channelCount
        }
    }

    private fun MediaFormat.getIntegerOrDefault(key: String, defaultValue: Int): Int {
        return if (containsKey(key)) getInteger(key) else defaultValue
    }

    private companion object {
        const val DEFAULT_MAX_DURATION_MS = 180_000L
        const val TIMEOUT_US = 10_000L
        const val SHORT_BYTES = 2
        const val FLOAT_BYTES = 4
        const val SHORT_SCALE = 32768f
    }
}
