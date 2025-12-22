package commands

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SimpleCommandsTest {
    private fun runCommand(cmd: Command, argv: List<String> = emptyList()): String {
        val output = StringBuilder()
        cmd.exec(argv) { output.append(it) }
        return output.toString()
    }

    @Test
    fun echoReturnsInput() {
        val result = runCommand(Echo, listOf("hello", "world"))
        assertEquals("hello world", result)
    }

    @Test
    fun echoEmptyReturnsEmpty() {
        val result = runCommand(Echo)
        assertEquals("", result)
    }

    @Test
    fun whoamiReturnsUser() {
        val result = runCommand(Whoami)
        assertEquals("tv", result)
    }

    @Test
    fun treeContainsFiles() {
        val result = runCommand(Tree)
        assertTrue(result.contains("├──") || result.contains("└──"))
    }

    @Test
    fun base64EncodeWorks() {
        val result = runCommand(Base64Cmd, listOf("encode", "hello"))
        assertEquals("aGVsbG8=", result)
    }

    @Test
    fun base64DecodeWorks() {
        val result = runCommand(Base64Cmd, listOf("decode", "aGVsbG8="))
        assertEquals("hello", result)
    }
}

