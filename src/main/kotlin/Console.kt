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

const val space = " "
const val br = "<br/>"

interface Command {
    val help: String
    fun exec(argv: List<String>, print: (String) -> Unit) {}
    fun complete(argv: List<String>): List<String>
}

object Console {
    var preventDefault = true
    private var console: Element? = null
    private var currentLine: Element? = null
    private var currentBody: Element? = null
    private var historyOffset: Int? = null
    private var consoleLineTemplate: Node? = null
    private var tmpCmd: String? = null
    private val history = mutableListOf<String>()

    fun init() {
        console = document.getElementById(consoleId)
        currentLine = document.getElementsByClassName(consoleLine)[1]
        currentBody = currentLine!!.getElementsByClassName(consoleBody)[0]
        consoleLineTemplate = currentLine!!.cloneNode(true)
    }

    fun clear() {
        while (console!!.lastChild != null) {
            console!!.removeChild(console!!.lastChild!!)
        }
    }

    fun backspace() = setLine(getLine().dropLast(1))
    fun tab() = complete(getLine())
    fun enter() = exec(getLine())
    fun up() {
        if (history.isNotEmpty()) {
            if (historyOffset == null) {
                tmpCmd = getLine()
                historyOffset = history.size
            }
            historyOffset = max(0, historyOffset!! - 1)
            setLine(history[historyOffset!!])
        }
    }

    fun down() {
        if (historyOffset != null && historyOffset!! < history.size - 1) {
            historyOffset = min(history.size, historyOffset!! + 1)
            setLine(history[historyOffset!!])
        }
    }

    fun input(event: KeyboardEvent) {
        if (event.key.length == 1 && !event.ctrlKey && !event.altKey && !event.metaKey) {
            setLine(getLine() + event.key)
        } else {
            preventDefault = false
        }
    }

    private fun removeCurrentLineCursor() = this.currentLine!!.classList.remove("active")
    private fun scrollDown() = window.scroll(0.0, document.body!!.scrollHeight.toDouble())
    private fun getLine(): String = currentBody!!.textContent!!
    private fun setLine(line: String) {
        currentBody!!.textContent = line
    }

    private fun parse(input: String): Pair<String, List<String>> {
        val parts = input.trim().split("\\s+".toRegex())
        return Pair(parts.first(), parts.drop(1))
    }

    private fun newPrompt(content: String? = null) {
        this.currentLine = consoleLineTemplate!!.cloneNode(true) as Element?
        this.currentBody = this.currentLine!!.getElementsByClassName(consoleBody)[0]
        setLine((content ?: getLine()))
        this.console!!.append(currentLine)
    }

    private fun setupStdout(): Element {
        val stdout = document.create.div().apply { classList.add("stdout") }
        this.console!!.append(stdout)
        return stdout
    }

    private fun complete(input: String) {
        var prefix = ""
        var choices = listOf<String>()
        val (cmd, argv) = parse(input)
        val command = commands[cmd]

        if (command != null && command.complete(argv).isNotEmpty()) {
            prefix = "$cmd "
            choices = command.complete(argv)
        } else if (argv.isEmpty()) {
            choices = commands["help"]?.complete(listOf(cmd))!!
        }

        if (choices.size == 1) {
            setLine(prefix + choices.first())
        } else if (choices.size > 1) {
            removeCurrentLineCursor()
            val stdout = setupStdout()
            choices.forEachIndexed { i, it ->
                if (i != 0) {
                    stdout.innerHTML += br
                }
                stdout.innerHTML += it
            }
            newPrompt(getLine())
        }
    }

    private fun exec(input: String, silent: Boolean = false) {
        tmpCmd = null
        historyOffset = null
        removeCurrentLineCursor()
        val line = input.trim()
        if (line.isNotEmpty()) {
            if (!silent && line !in history) {
                history.add(line)
            }
            val stdout = setupStdout()
            val print: (String) -> Unit = {
                stdout.innerHTML += it
                scrollDown()
            }
            val (cmd, argv) = parse(input)
            commands[cmd]?.exec(argv, print) ?: print("command not found: $cmd")
        }
        newPrompt()
    }

    object Help : Command {
        override val help = "this command"
        override fun exec(argv: List<String>, print: (String) -> Unit) {
            val cmds = commands.keys.toList()
            for (i in cmds.indices) {
                if (i > 0) {
                    print(br)
                }
                print("${cmds[i]}: ${commands[cmds[i]]!!.help}")
            }
        }

        override fun complete(argv: List<String>): List<String> {
            val choices = mutableListOf<String>()
            for (i in 0..commands.keys.size) {
                if (history[i].indexOf(argv[0]) == 0 || history[i].indexOf(" ") == 0) {
                    choices.add(history[i])
                }
            }
            return choices
        }
    }
}
