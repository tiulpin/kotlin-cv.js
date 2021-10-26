import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.*
import kotlinx.html.dom.*

const val consoleId = "console"
const val consoleLine = "console-line"
const val consoleBody = "body"

fun main() {
    document.body!!.append.div {
        id = consoleId
        div(consoleLine) {
            p {
                +"🚀 This website is powered by Kotlin/JS."
            }
        }
        div("$consoleLine active") {
            div("prompt") {
                pre {
                    +"tiulp.in › "
                }
            }
            div(consoleBody)
        }
    }
    Console.init()
    window.onkeydown = {
        when (it.key) {
            "Backspace" -> Console.backspace()
            "Tab" -> Console.tab()
            "Enter" -> Console.enter()
            "ArrowUp" -> Console.up()
            "ArrowDown" -> Console.down()
            else -> Console.input(it)
        }
        if (Console.preventDefault) {
            it.preventDefault()
        }
    }
}
