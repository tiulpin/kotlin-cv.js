package commands

import kotlin.test.Test
import kotlin.test.assertTrue

class GrepTest {
    private fun runCommand(cmd: Command, argv: List<String>, stdin: String? = null): String {
        val output = StringBuilder()
        cmd.exec(argv, { output.append(it) }, stdin)
        return output.toString()
    }

    @Test
    fun grepFindsPattern() {
        val result = runCommand(Grep, listOf("hello"), stdin = "hello world\ngoodbye world")
        assertTrue(result.contains("hello"))
    }

    @Test
    fun grepNoMatchReturnsNoMatches() {
        val result = runCommand(Grep, listOf("xyz"), stdin = "hello world")
        assertTrue(result.contains("no matches"))
    }

    @Test
    fun grepIsCaseInsensitive() {
        val result = runCommand(Grep, listOf("HELLO"), stdin = "hello world")
        assertTrue(result.contains("hello"))
    }
}


