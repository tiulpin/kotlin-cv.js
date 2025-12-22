package commands

import env
import files
import host
import kotlinx.browser.window
import kotlin.js.Date

private fun outputCommand(helpText: String, output: () -> String) = object : Command {
    override val help = helpText
    override fun exec(argv: List<String>, print: (String) -> Unit) = print(output())
}

private fun actionCommand(helpText: String, action: () -> Unit) = object : Command {
    override val help = helpText
    override fun exec(argv: List<String>, print: (String) -> Unit) = action()
}

private fun readOnlyCommand(name: String, verb: String, needsDest: Boolean = false) = object : Command {
    override val help = "$name (read-only filesystem)"
    override fun complete(argv: List<String>) = listFiles(argv)
    override fun exec(argv: List<String>, print: (String) -> Unit) {
        val f = argv.filter { !it.startsWith("-") }
        print(when {
            f.isEmpty() -> "$name: missing file operand"
            needsDest && f.size == 1 -> "$name: missing destination file operand after '${f.first()}'"
            needsDest -> "$name: cannot $verb '${f.first()}' to '${f.last()}': Read-only file system"
            else -> "$name: cannot $verb '${f.first()}': Read-only file system"
        })
    }
}

@JsName("encodeURIComponent") private external fun encodeURIComponent(str: String): String
@JsName("decodeURIComponent") private external fun decodeURIComponent(str: String): String

val DateCmd = outputCommand("print the current date and time") { Date().toString() }
val Whoami = outputCommand("print the current user") { "tv" }
val Pwd = outputCommand("print the current working directory") { "https://$host/" }

val Printenv = object : Command {
    override val help = "print all environment variables"
    override fun exec(argv: List<String>, print: (String) -> Unit) = env.forEach { (k, v) -> print("$k=$v$br") }
}

val History = object : Command {
    override val help = "show the command history"
    override fun exec(argv: List<String>, print: (String) -> Unit) =
        Console.history.forEachIndexed { i, cmd -> print("${if (i > 0) br else ""}$i $cmd") }
}

object Uptime : Command {
    init { js("if (!window.__uptimeStart) { window.__uptimeStart = Date.now(); }") }
    override val help = "show how long you've been in this terminal"
    override fun exec(argv: List<String>, print: (String) -> Unit) {
        val elapsed = ((Date.now() - (js("window.__uptimeStart") as Double)) / 1000).toLong()
        val h = elapsed / 3600; val m = (elapsed % 3600) / 60; val s = elapsed % 60
        val parts = buildList {
            if (h > 0) add("$h hour${if (h != 1L) "s" else ""}")
            if (m > 0) add("$m minute${if (m != 1L) "s" else ""}")
            add("$s second${if (s != 1L) "s" else ""}")
        }
        print("up ${parts.joinToString(", ")}")
    }
}

val Echo = object : Command {
    override val help = "echo [text ...]"
    override fun exec(argv: List<String>, print: (String) -> Unit) = print(argv.joinToString(" "))
}

val Clear = actionCommand("clear the terminal screen") { Console.clear() }
val Exit = actionCommand("exit the shell") { window.close() }
val Touch = readOnlyCommand("touch", "touch")
val Rm = readOnlyCommand("rm", "remove")
val Cp = readOnlyCommand("cp", "create regular file", needsDest = true)
val Mv = readOnlyCommand("mv", "move", needsDest = true)
val Mkdir = readOnlyCommand("mkdir", "create directory")

val Cd = object : Command {
    override val help = "cd (read-only filesystem)"
    override fun exec(argv: List<String>, print: (String) -> Unit) =
        print(if (argv.isEmpty()) help else "cd: Read-only file system")
}

val Tree = object : Command {
    override val help = "list contents in tree format"
    override fun exec(argv: List<String>, print: (String) -> Unit) {
        val tree = files.mapIndexed { i, f -> "${if (i == files.lastIndex) "└" else "├"}── ${f.name}$br" }
        print(".$br${tree.joinToString("")}")
    }
}

val Uuid = outputCommand("generate a random UUID v4") { js("crypto.randomUUID()").toString() }

val Base64Cmd = object : Command {
    override val help = "base64 <encode|decode> <text>"
    override fun complete(argv: List<String>) =
        if (argv.size <= 1) listOf("encode", "decode").filter { it.startsWith(argv.lastOrNull() ?: "") } else emptyList()
    override fun exec(argv: List<String>, print: (String) -> Unit) {
        if (argv.size < 2) return print(help)
        val text = argv.drop(1).joinToString(" ")
        print(when (argv[0].lowercase()) {
            "encode", "e" -> window.btoa(text)
            "decode", "d" -> runCatching { window.atob(text) }.getOrDefault("base64: invalid input")
            else -> "base64: use 'encode' or 'decode'"
        })
    }
}

val Json = object : Command {
    override val help = "json <json-string> - pretty-print JSON"
    override fun exec(argv: List<String>, print: (String) -> Unit) {
        if (argv.isEmpty()) return print(help)
        runCatching {
            val pretty = JSON.stringify(JSON.parse<Any>(argv.joinToString(" ")), null, 2)
            print("<pre>${escapeHtml(pretty)}</pre>")
        }.onFailure { print("json: invalid JSON") }
    }
}

val UrlEncode = object : Command {
    override val help = "urlencode <text>"
    override fun exec(argv: List<String>, print: (String) -> Unit) =
        if (argv.isEmpty()) print(help) else print(encodeURIComponent(argv.joinToString(" ")))
}

val UrlDecode = object : Command {
    override val help = "urldecode <text>"
    override fun exec(argv: List<String>, print: (String) -> Unit) =
        if (argv.isEmpty()) print(help) else print(runCatching { decodeURIComponent(argv.joinToString(" ")) }.getOrDefault("urldecode: invalid"))
}

val Color = object : Command {
    override val help = "color <hex|name> - preview color"
    override fun exec(argv: List<String>, print: (String) -> Unit) {
        if (argv.isEmpty()) return print(help)
        val c = argv.joinToString(" ")
        print("""<span style="display:inline-block;width:60px;height:20px;background:$c;border:1px solid #555;vertical-align:middle"></span> <code>$c</code>""")
    }
}

object Open : Command {
    override val help = "open <url> - open URL in new tab"
    override fun exec(argv: List<String>, print: (String) -> Unit) {
        if (argv.isEmpty()) return print(help)
        val url = argv.joinToString(" ").let { if (it.startsWith("http")) it else "https://$it" }
        window.open(url, blank)?.focus()
        print("opening $url")
    }
}

val Sandwich = outputCommand("make a sandwich") { "what? make it yourself." }

val Neofetch = object : Command {
    override val help = "system information"
    override fun exec(argv: List<String>, print: (String) -> Unit) {
        val info = buildString {
            append("<pre style=\"line-height:1.3\">")
            append("       <span style=\"color:#61afef\">user</span>@<span style=\"color:#61afef\">$host</span>$br")
            append("       ---------------$br")
            append("  <span style=\"color:#e06c75\">▄▄▄▄</span> <span style=\"color:#888\">OS:</span> Kotlin/JS$br")
            append(" <span style=\"color:#e06c75\">▄████</span> <span style=\"color:#888\">Shell:</span> kotlin-cv.js$br")
            append(" <span style=\"color:#e06c75\">██▀▀▀</span> <span style=\"color:#888\">Terminal:</span> Browser$br")
            append("  <span style=\"color:#e06c75\">▀▀▀▀</span> <span style=\"color:#888\">Uptime:</span> ${Date().toLocaleTimeString()}$br")
            append("</pre>")
        }
        print(info)
    }
}

val Man = object : Command {
    override val help = "man <command> - show help for command"
    override fun complete(argv: List<String>) = commands.keys.filter { it.startsWith(argv.lastOrNull() ?: "") }
    override fun exec(argv: List<String>, print: (String) -> Unit) {
        if (argv.isEmpty()) return print(help)
        val cmd = commands[argv[0]]
        if (cmd != null) print("${b(argv[0])}: ${cmd.help}")
        else print("man: no manual entry for ${argv[0]}")
    }
}

