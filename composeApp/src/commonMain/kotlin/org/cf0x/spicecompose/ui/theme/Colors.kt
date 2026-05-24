package org.cf0x.spicecompose.ui.theme

import androidx.compose.ui.graphics.Color

data class KeyColorOption(val color: Color, val nameEn: String, val nameZh: String)

val keyColorPresets = listOf(
    KeyColorOption(Color(0xFFF44336), "Red",        "红色"),
    KeyColorOption(Color(0xFFE91E63), "Pink",       "粉色"),
    KeyColorOption(Color(0xFF9C27B0), "Purple",     "紫色"),
    KeyColorOption(Color(0xFF673AB7), "Deep Purple","深紫"),
    KeyColorOption(Color(0xFF3F51B5), "Indigo",     "靛蓝"),
    KeyColorOption(Color(0xFF2196F3), "Blue",       "蓝色"),
    KeyColorOption(Color(0xFF00BCD4), "Cyan",       "青色"),
    KeyColorOption(Color(0xFF009688), "Teal",       "蓝绿"),
    KeyColorOption(Color(0xFF4CAF50), "Green",      "绿色"),
    KeyColorOption(Color(0xFFFFEB3B), "Yellow",     "黄色"),
    KeyColorOption(Color(0xFFFFC107), "Amber",      "琥珀"),
    KeyColorOption(Color(0xFFFF9800), "Orange",     "橙色"),
    KeyColorOption(Color(0xFF795548), "Brown",      "棕色"),
    KeyColorOption(Color(0xFF607D8B), "Blue Grey",  "蓝灰"),
    KeyColorOption(Color(0xFFFF9CA8), "Light Pink", "浅粉"),
)

val defaultKeyColor = Color(0xFF3F51B5)
