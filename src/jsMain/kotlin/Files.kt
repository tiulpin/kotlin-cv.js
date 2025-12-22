import commands.b
import commands.br
import commands.h

data class File(
    val name: String,
    val content: String,
    val owner: String = "tv",
    val size: Int = content.length,
    val date: String = "1970-01-01",
    val permissions: String = "rw-r--r--",
)

val hey = File(
    "hey.html", """
                👋 Welcome to ${b("kotlin-cv.js")} — a terminal-style personal website template 
                built with ${h("Kotlin/JS", "https://kotlinlang.org/docs/js-overview.html")}. $br$br

                ${b("Getting Started:")}$br
                • Edit ${b("Files.kt")} to customize this intro and add your files$br
                • Edit ${b("Main.kt")} to change the hostname in the prompt$br
                • Run ${b("cat quote.txt")} or ${b("ls")} to explore$br
                • Type ${b("help")} to see all available commands$br$br

                ${h("GitHub", "https://github.com/tiulpin/kotlin-cv.js")}
                · ${h("Live Example", "https://tiulp.in")}
                · ${h("Kotlin/JS Docs", "https://kotlinlang.org/docs/js-overview.html")}
    """.trimIndent()
)

val quoteTxt = File(
    "quote.txt", """
                "A human being should be able to change a diaper, plan an invasion, butcher a hog, conn a ship,
                design a building, write a sonnet, balance accounts, build a wall, set a bone, comfort the dying,
                take orders, give orders, cooperate, act alone, solve equations, analyze a new problem,
                pitch manure, program a computer, cook a tasty meal, fight efficiently, die gallantly.
                <a>Specialization is for insects.</a>"$br$br ― Robert A. Heinlein.
    """.trimIndent()
)

val files = listOf(hey, quoteTxt)
