package com.limbe.hexamusicplayer

import com.limbe.hexamusicplayer.infrastructure.lyrics.LrcLyricParser
import org.junit.Assert.assertEquals
import org.junit.Test

class LrcLyricParserTest {

    private val parser = LrcLyricParser()

    @Test
    fun `parses multiple timestamps and sorts lyric lines`() {
        val parsed = parser.parse(
            """
            [00:10.00]first line
            [00:05.50][00:15.00]echo line
            """.trimIndent()
        )

        assertEquals(3, parsed.size)
        assertEquals(5_500L, parsed[0].timeMs)
        assertEquals("echo line", parsed[0].text)
        assertEquals(10_000L, parsed[1].timeMs)
        assertEquals("first line", parsed[1].text)
        assertEquals(15_000L, parsed[2].timeMs)
    }
}
