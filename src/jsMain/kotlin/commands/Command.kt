package commands

interface Command {
    val help: String
    fun exec(argv: List<String>, print: (String) -> Unit) {}
    fun exec(argv: List<String>, print: (String) -> Unit, stdin: String?) = exec(argv, print)
    fun complete(argv: List<String>): List<String> = emptyList()
}


