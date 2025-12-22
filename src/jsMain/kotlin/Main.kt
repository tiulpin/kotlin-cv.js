import commands.Console
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.*
import kotlinx.html.dom.*

const val host = "tiulp.in"

fun main() {
    document.body!!.append.div {
        id = "console"
        div("console-line") {
            p { unsafe { +files.first().content } }
        }
        div("console-line active") {
            div("prompt") { pre { +"$host › " } }
            div("body")
        }
    }
    Console.init()
    Console.setLine("tree")
    Console.enter()

    window.onkeydown = { event ->
        Console.preventDefault = true
        when (event.key) {
            "c" if event.ctrlKey && !event.metaKey -> Console.ctrlC()
            "Backspace" -> Console.backspace()
            "Tab" -> Console.tab()
            "Enter" -> Console.enter()
            "ArrowUp" -> Console.up()
            "ArrowDown" -> Console.down()
            else -> Console.input(event)
        }
        if (Console.preventDefault) event.preventDefault()
    }

    document.onpaste = { event ->
        val pastedText = event.asDynamic().clipboardData?.getData("text") as? String
        if (pastedText != null) {
            Console.setLine(Console.getLine() + pastedText.replace("\n", " ").replace("\r", ""))
            event.preventDefault()
        }
    }
}

