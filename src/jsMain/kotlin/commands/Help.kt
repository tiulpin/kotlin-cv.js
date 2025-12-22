package commands

object Help : Command {
    override val help = "this command"

    override fun exec(argv: List<String>, print: (String) -> Unit) {
        commands.entries.forEachIndexed { i, (name, cmd) ->
            if (cmd.help.isEmpty()) return@forEachIndexed
            if (i > 0) print(br)
            print("${b(name)}: ${cmd.help}")
        }
    }

    override fun complete(argv: List<String>): List<String> {
        val prefix = argv.firstOrNull() ?: ""
        return Console.history.filter { it.startsWith(prefix) }.distinct()
    }
}


