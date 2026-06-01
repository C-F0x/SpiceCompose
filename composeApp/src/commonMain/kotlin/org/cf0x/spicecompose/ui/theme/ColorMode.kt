package org.cf0x.spicecompose.ui.theme

enum class ColorMode(val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2);

    companion object {
        fun fromValue(v: Int) = entries.firstOrNull { it.value == v } ?: SYSTEM
    }
}
