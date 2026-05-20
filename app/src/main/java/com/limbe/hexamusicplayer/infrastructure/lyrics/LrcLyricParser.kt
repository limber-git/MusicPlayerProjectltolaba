package com.limbe.hexamusicplayer.infrastructure.lyrics

import com.limbe.hexamusicplayer.domain.model.LyricLine

class LrcLyricParser {

    fun parse(rawLrc: String): List<LyricLine> {
        return rawLrc
            .lineSequence()
            .flatMap(::parseLine)
            .sortedBy { it.timeMs }
            .toList()
    }

    private fun parseLine(line: String): Sequence<LyricLine> {
        val matches = TIMESTAMP_REGEX.findAll(line).toList()
        if (matches.isEmpty()) return emptySequence()

        val lyricText = line.replace(TIMESTAMP_REGEX, "").trim()
        return matches.asSequence().mapNotNull { match ->
            val minutes = match.groups["minutes"]?.value?.toLongOrNull() ?: return@mapNotNull null
            val seconds = match.groups["seconds"]?.value?.toLongOrNull() ?: return@mapNotNull null
            val hundredths = match.groups["hundredths"]?.value?.toLongOrNull() ?: 0L

            LyricLine(
                timeMs = (minutes * 60_000L) + (seconds * 1_000L) + (hundredths * 10L),
                text = lyricText
            )
        }
    }

    private companion object {
        val TIMESTAMP_REGEX = Regex("""\[(?<minutes>\d{2}):(?<seconds>\d{2})(?:\.(?<hundredths>\d{2}))?]""")
    }
}
