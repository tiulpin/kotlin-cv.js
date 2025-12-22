package commands

import files

object Cat : Command {
    override val help = "usage: cat [file ...]"
    override fun complete(argv: List<String>) = listFiles(argv)

    override fun exec(argv: List<String>, print: (String) -> Unit, stdin: String?) {
        if (stdin != null && argv.isEmpty()) return print(stdin)
        if (argv.isEmpty()) return print(help)
        val file = files.find { it.name == argv[0] }
        if (file != null) print("<p>${file.content}</p>") else print("cat: ${argv[0]}: No such file or directory")
    }
}


