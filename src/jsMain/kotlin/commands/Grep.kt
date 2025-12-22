package commands

import files

object Grep : Command {
    override val help = "grep <pattern> [file] - search for pattern"
    override fun complete(argv: List<String>) = if (argv.size >= 1) listFiles(argv.drop(1)) else emptyList()

    override fun exec(argv: List<String>, print: (String) -> Unit, stdin: String?) {
        if (argv.isEmpty()) return print(help)
        val pattern = argv[0]
        val regex = Regex(pattern, RegexOption.IGNORE_CASE)
        val text = stdin ?: files.find { it.name == argv.getOrNull(1) }?.content
            ?: return print("grep: ${argv.getOrNull(1) ?: "no input"}: No such file or directory")

        val matches = text.lines().filter { regex.containsMatchIn(it) }
        if (matches.isEmpty()) print("(no matches)")
        else print(matches.joinToString(br) { escapeHtml(it) })
    }
}

