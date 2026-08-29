package org.cf0x.spicecompose.util

/** Minimal cross-platform formatter for the placeholders used by the app. */
fun String.simpleFormat(vararg args: Any?): String {
    var result = this
    var index = 0
    // Replace %s placeholders.
    while (true) {
        val idx = result.indexOf("%s")
        if (idx < 0 || index >= args.size) break
        result = result.substring(0, idx) + args[index].toString() + result.substring(idx + 2)
        index++
    }
    // Replace the indexed integer placeholder.
    args.getOrNull(0)?.let { first ->
        result = result.replace("%1\$d", first.toString())
    }
    // Unescape percent signs.
    return result.replace("%%", "%")
}
