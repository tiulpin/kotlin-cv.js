val commands = mapOf(
    "help" to Console.Help,
    "ls" to object : Command {
        override val help = "usage: ls (no additional flags supported)"
        override fun complete(argv: List<String>): List<String> = listOf()
        override fun exec(argv: List<String>, print: (String) -> Unit) {
            if (argv.isEmpty()) {
                files.forEachIndexed { i, it ->
                    if (argv.isEmpty() || argv[0] == it.name) {
                        if (i > 0) {
                            print(space)
                        }
                        print(it.name)
                    }
                }
            }
            else {
                print(help)
            }
        }
    },
    "cat" to object : Command {
        override val help = "usage: cat [file ...]"
        override fun complete(argv: List<String>): List<String> =
            files.filter { it.name.indexOf(argv[0]) == 0 || it.name.indexOf(" ") == 0 }.map { it.name }

        override fun exec(argv: List<String>, print: (String) -> Unit) {
            if (argv.isNotEmpty()) {
                files.forEach {
                    if (it.name == argv[0]) {
                        print("<p>${it.content}</p>")
                        return
                    }
                }
                print("${argv[0]}: no such file")
            }
            else {
                print(help)
            }
        }
    },
    "clear" to object : Command {
        override val help = "clear the terminal screen"
        override fun complete(argv: List<String>): List<String> = listOf()
        override fun exec(argv: List<String>, print: (String) -> Unit) = Console.clear()
    }
)