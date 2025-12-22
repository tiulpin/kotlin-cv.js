package commands

import files

private fun headTailCommand(name: String, takeFn: (List<String>, Int) -> List<String>) = object : Command {
    override val help = "$name [-n count] [file]"
    override fun complete(argv: List<String>) = listFiles(argv.filter { !it.startsWith("-") && !it.all { c -> c.isDigit() } })

    override fun exec(argv: List<String>, print: (String) -> Unit, stdin: String?) {
        val nIndex = argv.indexOf("-n")
        val n = if (nIndex >= 0) argv.getOrNull(nIndex + 1)?.toIntOrNull() ?: 10 else 10
        val fileArg = argv.filterNot { it == "-n" || it.toIntOrNull() != null }.firstOrNull()

        val text = stdin ?: fileArg?.let { name -> files.find { it.name == name }?.content }
            ?: return print(if (fileArg != null) "$name: $fileArg: No such file or directory" else help)

        val lines = text.lines()
        print(takeFn(lines, n).joinToString(br) { escapeHtml(it) })
    }
}

val Head = headTailCommand("head") { lines, n -> lines.take(n) }
val Tail = headTailCommand("tail") { lines, n -> lines.takeLast(n) }

