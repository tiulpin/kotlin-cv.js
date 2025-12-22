package commands

import File
import files

object Ls : Command {
    override val help = "list files. flags: -l (long) -a (all) -h (human sizes) -r (reverse) -t (time sort) -S (size sort) -1 (one per line)"

    private val hiddenFiles = listOf(
        File(".", "current directory", date = "2024-01-01"),
        File("..", "parent directory", date = "2024-01-01"),
    )

    private val flags = listOf("-l", "-a", "-h", "-r", "-t", "-S", "-1", "-la", "-lah", "-lh", "-al")
    override fun complete(argv: List<String>) = argv.lastOrNull()?.takeIf { it.startsWith("-") }?.let { last -> flags.filter { it.startsWith(last) } } ?: emptyList()

    override fun exec(argv: List<String>, print: (String) -> Unit) {
        val flagChars = mutableSetOf<Char>()
        argv.filter { it.startsWith("-") }.forEach { arg ->
            arg.drop(1).forEach { flagChars.add(it) }
        }

        val longFormat = 'l' in flagChars
        val showAll = 'a' in flagChars
        val humanReadable = 'h' in flagChars
        val reverse = 'r' in flagChars
        val sortByTime = 't' in flagChars
        val sortBySize = 'S' in flagChars
        val onePerLine = '1' in flagChars

        var fileList = if (showAll) {
            hiddenFiles + files
        } else {
            files
        }

        fileList = when {
            sortByTime -> fileList.sortedBy { it.date }
            sortBySize -> fileList.sortedBy { it.size }
            else -> fileList.sortedBy { it.name }
        }

        if (reverse) {
            fileList = fileList.reversed()
        }

        if (longFormat) {
            val maxSizeWidth = fileList.maxOfOrNull {
                formatSize(it.size, humanReadable).length 
            } ?: 1
            val maxOwnerWidth = fileList.maxOfOrNull { it.owner.length } ?: 2

            val lines = fileList.map { file ->
                val sizeStr = formatSize(file.size, humanReadable).padStart(maxSizeWidth)
                val ownerStr = file.owner.padEnd(maxOwnerWidth)
                val typeChar = if (file.name == "." || file.name == "..") 'd' else '-'
                
                // Format: -rw-r--r-- tv  1234 2024-01-01 filename
                "<span style=\"color:#888\">$typeChar${file.permissions}</span> " +
                "<span style=\"color:#e94560\">$ownerStr</span> " +
                "<span style=\"color:#00fff5\">$sizeStr</span> " +
                "<span style=\"color:#888\">${file.date}</span> " +
                colorFileName(file.name)
            }
            print("<pre style=\"margin:0;line-height:1.4\">${lines.joinToString("\n")}</pre>")
        } else if (onePerLine) {
            fileList.forEachIndexed { i, file ->
                if (i > 0) print(br)
                print(colorFileName(file.name))
            }
        } else {
            fileList.forEachIndexed { i, file ->
                if (i > 0) print(space)
                print(colorFileName(file.name))
            }
        }
    }

    private fun formatSize(bytes: Int, human: Boolean): String {
        if (!human) return bytes.toString()
        
        return when {
            bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)}M"
            bytes >= 1024 -> "${bytes / 1024}K"
            else -> bytes.toString()
        }
    }

    private fun colorFileName(name: String): String {
        return when {
            name == "." || name == ".." -> "<span style=\"color:#0f3460\">$name</span>"
            name.startsWith(".") -> "<span style=\"color:#666\">$name</span>"
            name.endsWith(".md") || name.endsWith(".txt") -> "<span style=\"color:#eaeaea\">$name</span>"
            name.endsWith(".html") -> "<span style=\"color:#e94560\">$name</span>"
            name.endsWith(".log") -> "<span style=\"color:#666\">$name</span>"
            else -> name
        }
    }
}


