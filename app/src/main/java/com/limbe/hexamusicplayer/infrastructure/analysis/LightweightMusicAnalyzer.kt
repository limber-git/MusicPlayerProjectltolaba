package com.limbe.hexamusicplayer.infrastructure.analysis

import com.limbe.hexamusicplayer.domain.model.AnalysisSource
import com.limbe.hexamusicplayer.domain.model.ChordEvent
import com.limbe.hexamusicplayer.domain.model.DetectedNote
import com.limbe.hexamusicplayer.domain.model.KeyEstimate
import com.limbe.hexamusicplayer.domain.model.KeyMode
import com.limbe.hexamusicplayer.domain.model.MusicAnalysisState
import com.limbe.hexamusicplayer.domain.model.MusicAnalysisStatus
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

class LightweightMusicAnalyzer {

    fun analyze(audioData: PcmAudioData, trackId: Long): MusicAnalysisState {
        if (audioData.samples.size < audioData.sampleRate) {
            error("Not enough decoded audio to analyze")
        }

        val frames = collectPitchFrames(audioData)
        if (frames.isEmpty()) {
            error("Could not detect stable pitch frames")
        }

        val globalChroma = FloatArray(NOTE_NAMES.size)
        frames.forEach { frame ->
            globalChroma[frame.pitchClass] += frame.confidence
        }

        val keyEstimate = estimateKey(globalChroma)
        val currentNote = frames.maxByOrNull { it.confidence }?.toDetectedNote()
        val chordEvents = estimateChordEvents(frames, audioData.durationMs)

        return MusicAnalysisState(
            trackId = trackId,
            status = MusicAnalysisStatus.ESTIMATED,
            keyEstimate = keyEstimate,
            currentNote = currentNote,
            chordEvents = chordEvents
        )
    }

    private fun collectPitchFrames(audioData: PcmAudioData): List<PitchFrame> {
        val windowSize = chooseWindowSize(audioData.sampleRate)
        val hopSize = windowSize
        if (audioData.samples.size < windowSize) return emptyList()

        val frames = mutableListOf<PitchFrame>()
        var offset = 0
        while (offset + windowSize <= audioData.samples.size) {
            estimatePitch(audioData.samples, offset, windowSize, audioData.sampleRate)?.let { estimate ->
                val startMs = ((offset.toDouble() / audioData.sampleRate) * 1000.0).toLong()
                frames += PitchFrame(
                    startMs = startMs,
                    frequency = estimate.frequency,
                    pitchClass = frequencyToPitchClass(estimate.frequency),
                    midiNote = frequencyToMidi(estimate.frequency),
                    confidence = estimate.confidence
                )
            }
            offset += hopSize
        }
        return frames
    }

    private fun estimatePitch(
        samples: FloatArray,
        offset: Int,
        windowSize: Int,
        sampleRate: Int
    ): PitchEstimate? {
        val rms = calculateRms(samples, offset, windowSize)
        if (rms < MIN_RMS) return null

        val minLag = (sampleRate / MAX_FREQUENCY_HZ).toInt().coerceAtLeast(1)
        val maxLag = (sampleRate / MIN_FREQUENCY_HZ).toInt().coerceAtMost(windowSize - 1)
        var bestLag = -1
        var bestCorrelation = 0f

        for (lag in minLag..maxLag) {
            var sum = 0f
            var leftEnergy = 0f
            var rightEnergy = 0f
            val compareCount = windowSize - lag

            var index = 0
            while (index < compareCount) {
                val left = samples[offset + index]
                val right = samples[offset + index + lag]
                sum += left * right
                leftEnergy += left * left
                rightEnergy += right * right
                index++
            }

            val normalized = if (leftEnergy > 0f && rightEnergy > 0f) {
                sum / sqrt(leftEnergy * rightEnergy)
            } else {
                0f
            }

            if (normalized > bestCorrelation) {
                bestCorrelation = normalized
                bestLag = lag
            }
        }

        if (bestLag <= 0 || bestCorrelation < MIN_CORRELATION) return null
        return PitchEstimate(
            frequency = sampleRate.toFloat() / bestLag,
            confidence = bestCorrelation.coerceIn(0f, 1f)
        )
    }

    private fun estimateKey(chroma: FloatArray): KeyEstimate {
        val total = chroma.sum().coerceAtLeast(0.0001f)
        var bestScore = Float.NEGATIVE_INFINITY
        var bestRoot = 0
        var bestMode = KeyMode.MAJOR

        NOTE_NAMES.indices.forEach { root ->
            val majorScore = scoreKey(chroma, root, MAJOR_PROFILE)
            if (majorScore > bestScore) {
                bestScore = majorScore
                bestRoot = root
                bestMode = KeyMode.MAJOR
            }

            val minorScore = scoreKey(chroma, root, MINOR_PROFILE)
            if (minorScore > bestScore) {
                bestScore = minorScore
                bestRoot = root
                bestMode = KeyMode.MINOR
            }
        }

        val confidence = (bestScore / total).coerceIn(0f, 1f)
        return KeyEstimate(
            tonic = NOTE_NAMES[bestRoot],
            mode = bestMode,
            confidence = confidence,
            source = AnalysisSource.ESTIMATED
        )
    }

    private fun estimateChordEvents(frames: List<PitchFrame>, durationMs: Long): List<ChordEvent> {
        if (frames.isEmpty()) return emptyList()

        val segmentMs = 4_000L
        val events = mutableListOf<ChordEvent>()
        var segmentStart = 0L

        while (segmentStart < durationMs) {
            val segmentEnd = segmentStart + segmentMs
            val segmentFrames = frames.filter { it.startMs in segmentStart until segmentEnd }
            val chord = estimateChord(segmentFrames)
            if (chord != null && chord.name != events.lastOrNull()?.chordName) {
                events += ChordEvent(
                    startMs = segmentStart,
                    endMs = segmentEnd.coerceAtMost(durationMs),
                    chordName = chord.name,
                    confidence = chord.confidence,
                    source = AnalysisSource.ESTIMATED
                )
            }
            segmentStart += segmentMs
        }

        return events.take(MAX_CHORD_EVENTS)
    }

    private fun estimateChord(frames: List<PitchFrame>): ChordEstimate? {
        if (frames.size < MIN_FRAMES_PER_CHORD) return null

        val chroma = FloatArray(NOTE_NAMES.size)
        frames.forEach { frame ->
            chroma[frame.pitchClass] += frame.confidence
        }

        val root = chroma.indices.maxByOrNull { chroma[it] } ?: return null
        val majorScore = triadScore(chroma, root, major = true)
        val minorScore = triadScore(chroma, root, major = false)
        val total = chroma.sum().coerceAtLeast(0.0001f)
        val isMajor = majorScore >= minorScore
        val score = max(majorScore, minorScore)
        val suffix = if (isMajor) "" else "m"

        if (score / total < MIN_CHORD_CONFIDENCE) return null
        return ChordEstimate(
            name = "${NOTE_NAMES[root]}$suffix",
            confidence = (score / total).coerceIn(0f, 1f)
        )
    }

    private fun scoreKey(chroma: FloatArray, root: Int, profile: FloatArray): Float {
        var score = 0f
        profile.indices.forEach { index ->
            score += chroma[(root + index) % NOTE_NAMES.size] * profile[index]
        }
        return score
    }

    private fun triadScore(chroma: FloatArray, root: Int, major: Boolean): Float {
        val third = if (major) 4 else 3
        return chroma[root] +
            chroma[(root + third) % NOTE_NAMES.size] * 0.85f +
            chroma[(root + 7) % NOTE_NAMES.size] * 0.75f
    }

    private fun PitchFrame.toDetectedNote(): DetectedNote {
        val midi = midiNote
        return DetectedNote(
            noteName = NOTE_NAMES[pitchClass],
            octave = (midi / 12) - 1,
            startMs = startMs,
            confidence = confidence,
            source = AnalysisSource.ESTIMATED
        )
    }

    private fun chooseWindowSize(sampleRate: Int): Int {
        var size = 1
        val target = (sampleRate * 0.09f).roundToInt().coerceAtLeast(2048)
        while (size < target) size *= 2
        return size.coerceAtMost(8192)
    }

    private fun calculateRms(samples: FloatArray, offset: Int, windowSize: Int): Float {
        var sum = 0f
        repeat(windowSize) { index ->
            val value = samples[offset + index]
            sum += value * value
        }
        return sqrt(sum / windowSize)
    }

    private fun frequencyToMidi(frequency: Float): Int {
        return (69 + 12 * (ln(frequency / 440f) / ln(2f))).roundToInt()
    }

    private fun frequencyToPitchClass(frequency: Float): Int {
        val midi = frequencyToMidi(frequency)
        return ((midi % NOTE_NAMES.size) + NOTE_NAMES.size) % NOTE_NAMES.size
    }

    private data class PitchEstimate(
        val frequency: Float,
        val confidence: Float
    )

    private data class PitchFrame(
        val startMs: Long,
        val frequency: Float,
        val pitchClass: Int,
        val midiNote: Int,
        val confidence: Float
    )

    private data class ChordEstimate(
        val name: String,
        val confidence: Float
    )

    private companion object {
        val NOTE_NAMES = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val MAJOR_PROFILE = floatArrayOf(1f, 0.15f, 0.45f, 0.15f, 0.8f, 0.55f, 0.1f, 0.9f, 0.15f, 0.5f, 0.15f, 0.45f)
        val MINOR_PROFILE = floatArrayOf(1f, 0.15f, 0.45f, 0.75f, 0.15f, 0.55f, 0.1f, 0.85f, 0.65f, 0.15f, 0.45f, 0.15f)
        const val MIN_FREQUENCY_HZ = 65f
        const val MAX_FREQUENCY_HZ = 1_050f
        const val MIN_RMS = 0.01f
        const val MIN_CORRELATION = 0.45f
        const val MIN_CHORD_CONFIDENCE = 0.38f
        const val MIN_FRAMES_PER_CHORD = 2
        const val MAX_CHORD_EVENTS = 96
    }
}
