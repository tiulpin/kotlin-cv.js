package commands

import files

const val blank = "_blank"
const val space = " "
const val br = "<br/>"

fun b(content: String) = "<b>$content</b>"
fun i(content: String) = "<i>$content</i>"
fun h(content: String, href: String) = "<a href=\"$href\" target=\"$blank\">$content</a>"

fun escapeHtml(text: String) = text
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")

fun listFiles(argv: List<String>): List<String> =
    if (argv.isEmpty()) files.map { it.name }
    else files.filter { it.name.startsWith(argv[0]) }.map { it.name }


