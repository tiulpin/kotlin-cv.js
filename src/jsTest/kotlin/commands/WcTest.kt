package commands

import kotlin.test.Test
import kotlin.test.assertTrue

class WcTest {
    private fun runCommand(cmd: Command, argv: List<String>, stdin: String? = null): String {
        val output = StringBuilder()
        cmd.exec(argv, { output.append(it) }, stdin)
        return output.toString()
    }

    @Test
    fun wcCountsWords() {
        val result = runCommand(Wc, listOf("-w"), stdin = "hello world foo")
        assertTrue(result.contains("3"))
    }

    @Test
    fun wcCountsLines() {
        val result = runCommand(Wc, listOf("-l"), stdin = "line1\nline2\nline3")
        assertTrue(result.contains("3"))
    }

    @Test
    fun wcCountsChars() {
        val result = runCommand(Wc, listOf("-c"), stdin = "hello")
        assertTrue(result.contains("5"))
    }

    @Test
    fun wcDefaultShowsAll() {
        val result = runCommand(Wc, emptyList(), stdin = "hello world")
        // Should contain line count, word count, and char count
        assertTrue(result.split(" ").size >= 3)
    }
}


