package commands

import files

object Wc : Command {
    override val help = "wc [-l|-w|-c] [file] - word, line, character count"
    override fun complete(argv: List<String>) = listFiles(argv.filter { !it.startsWith("-") })

    override fun exec(argv: List<String>, print: (String) -> Unit, stdin: String?) {
        val flags = argv.filter { it.startsWith("-") }.flatMap { it.drop(1).toList() }.toSet()
        val fileArg = argv.firstOrNull { !it.startsWith("-") }
        val text = stdin ?: fileArg?.let { name -> files.find { it.name == name }?.content }
            ?: return print(if (fileArg != null) "wc: $fileArg: No such file or directory" else help)

        val lines = text.count { it == '\n' } + if (text.isNotEmpty() && !text.endsWith('\n')) 1 else 0
        val words = text.split(Regex("\\s+")).count { it.isNotBlank() }
        val chars = text.length

        val parts = buildList {
            if (flags.isEmpty() || 'l' in flags) add("$lines")
            if (flags.isEmpty() || 'w' in flags) add("$words")
            if (flags.isEmpty() || 'c' in flags) add("$chars")
        }
        print(parts.joinToString(" "))
    }
}

