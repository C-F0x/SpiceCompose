package org.cf0x.spicecompose.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Text

@Composable fun SettingsPlaceholder() = PlaceholderScreen("こんにちは！ 🎛️")

@Composable
private fun PlaceholderScreen(greeting: String) {
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = greeting, fontSize = 32.sp)
    }
}
