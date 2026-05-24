package org.cf0x.spicecompose.ui.theme

enum class ColorMode(val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2),
    MONET_SYSTEM(3),
    MONET_LIGHT(4),
    MONET_DARK(5),
    DARK_AMOLED(6);

    val isDark: Boolean get() = this == DARK || this == MONET_DARK || this == DARK_AMOLED
    val isLight: Boolean get() = this == LIGHT || this == MONET_LIGHT
    val isMonet: Boolean get() = this == MONET_SYSTEM || this == MONET_LIGHT || this == MONET_DARK
    val isAmoled: Boolean get() = this == DARK_AMOLED

    companion object {
        fun fromValue(v: Int) = entries.firstOrNull { it.value == v } ?: SYSTEM
    }
}
