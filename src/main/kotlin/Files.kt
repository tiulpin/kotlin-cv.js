data class File(
    val name: String,
    val content: String
)

val files = listOf(
    File(
        "quote.txt", """
                "A human being should be able to change a diaper, plan an invasion, butcher a hog, conn a ship,
                design a building, write a sonnet, balance accounts, build a wall, set a bone, comfort the dying,
                take orders, give orders, cooperate, act alone, solve equations, analyze a new problem,
                pitch manure, program a computer, cook a tasty meal, fight efficiently, die gallantly.
                Specialization is for insects." ― Robert A. Heinlein.
    """.trimIndent()
    )
)