package commands

import host
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.div
import kotlinx.html.dom.create
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.get
import kotlin.math.max
import kotlin.math.min

private const val CONSOLE_ID = "console"
private const val CONSOLE_LINE = "console-line"
private const val CONSOLE_BODY = "body"

object Console {
    var preventDefault = true
    private var terminal: Element? = null
    private var currentLine: Element? = null
    private var currentBody: Element? = null
    private var historyOffset: Int? = null
    private var consoleLineTemplate: Node? = null
    private var tmpCmd: String? = null
    val history = mutableListOf<String>()

    fun init() {
        terminal = document.getElementById(CONSOLE_ID)
        currentLine = document.getElementsByClassName(CONSOLE_LINE).item(1) as? Element
        currentBody = currentLine?.getElementsByClassName(CONSOLE_BODY)?.item(0) as? Element
        consoleLineTemplate = currentLine?.cloneNode(true)
    }

    fun clear() {
        while (terminal?.lastChild != null) terminal!!.removeChild(terminal!!.lastChild!!)
    }

    fun backspace() = setLine(getLine().dropLast(1))

    fun tab() = complete(getLine())

    fun enter() = exec(getLine())

    fun ctrlC() {
        // Can be extended for interrupt handling
    }

    fun up() {
        if (history.isEmpty()) return
        if (historyOffset == null) { tmpCmd = getLine(); historyOffset = history.size }
        historyOffset = max(0, historyOffset!! - 1)
        setLine(history[historyOffset!!])
    }

    fun down() {
        if (historyOffset == null || historyOffset!! >= history.size - 1) return
        historyOffset = min(history.size, historyOffset!! + 1)
        setLine(history[historyOffset!!])
    }

    fun input(event: KeyboardEvent) {
        if (event.key.length == 1 && !event.ctrlKey && !event.metaKey) setLine(getLine() + event.key)
        else preventDefault = false
        scrollDown()
    }

    private fun removeCurrentLineCursor() = currentLine!!.classList.remove("active")

    fun scrollDown() { window.setTimeout({ window.scrollTo(0.0, document.body?.scrollHeight?.toDouble() ?: 0.0) }, 0) }

    fun getLine(): String = currentBody!!.textContent!!

    fun setLine(line: String) { currentBody!!.textContent = line }

    fun setPrompt(prompt: String = "$host › ") {
        currentLine!!.getElementsByClassName("prompt").item(0)!!.innerHTML = "<pre>$prompt</pre>"
    }

    private fun parse(input: String): Pair<String, List<String>> {
        val parts = input.trim().split("\\s+".toRegex())
        return parts.first() to parts.drop(1)
    }

    private fun newPrompt(content: String? = null) {
        currentLine = consoleLineTemplate!!.cloneNode(true) as Element?
        currentBody = currentLine!!.getElementsByClassName(CONSOLE_BODY)[0]
        setLine(content ?: getLine())
        terminal!!.append(currentLine)
    }

    fun showPrompt() {
        newPrompt()
        scrollDown()
    }

    private fun setupStdout() = document.create.div().apply { classList.add("stdout") }.also { terminal!!.append(it) }

    private fun complete(input: String) {
        val (cmd, argv) = parse(input)
        val command = commands[cmd]

        val (prefix, choices) = when {
            command != null && command.complete(argv).isNotEmpty() -> "$cmd " to command.complete(argv)
            command != null && argv.isEmpty() -> "" to (commands["help"]?.complete(listOf(cmd)) ?: emptyList())
            command == null && argv.isEmpty() -> "" to commands.keys.filter { it.startsWith(cmd) }
            else -> "" to emptyList()
        }

        when {
            choices.size == 1 -> setLine(prefix + choices.first())
            choices.size > 1 -> {
                removeCurrentLineCursor()
                setupStdout().innerHTML = choices.joinToString(br)
                newPrompt(getLine())
            }
        }
    }

    private fun exec(input: String, silent: Boolean = false) {
        tmpCmd = null
        historyOffset = null
        removeCurrentLineCursor()
        val line = input.trim()

        if (line.isNotEmpty()) {
            if (!silent && line !in history) history.add(line)

            val stdout = setupStdout()
            val print: (String) -> Unit = { output ->
                stdout.innerHTML += output
                scrollDown()
            }

            if (line.contains(">")) { print("$host: read-only file system"); newPrompt(); return }

            if (line.contains("|")) executePipeline(line, print)
            else {
                val (cmd, argv) = parse(input)
                commands[cmd]?.exec(argv, print, null) ?: print("command not found: $cmd")
            }
        }

        newPrompt()
    }

    private fun executePipeline(input: String, finalPrint: (String) -> Unit) {
        val segments = input.split("|").map { it.trim() }.filter { it.isNotEmpty() }
        if (segments.isEmpty()) return

        var stdin: String? = null
        for ((index, segment) in segments.withIndex()) {
            val (cmd, argv) = parse(segment)
            val command = commands[cmd]
                ?: return finalPrint("$cmd: command not found")

            if (index == segments.lastIndex) command.exec(argv, finalPrint, stdin)
            else {
                val output = StringBuilder()
                command.exec(argv, { output.append(it.replace(Regex("<br\\s*/?>"), "\n").replace(Regex("<[^>]+>"), "")) }, stdin)
                stdin = output.toString()
            }
        }
    }
}


