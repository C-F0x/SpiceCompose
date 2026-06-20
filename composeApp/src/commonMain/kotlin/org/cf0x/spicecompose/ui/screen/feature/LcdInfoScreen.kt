package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.LcdInfo
import org.cf0x.spicecompose.network.spiceapi.wrappers.lcdInfo
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.theme.ThemePreferences
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LcdInfoScreen(onBack: () -> Unit) {
    val cm = LocalConnectionManager.current
    val conn = cm.getClient()
    val fullscreen = LocalFullscreenMode.current
    val p = ThemePreferences
    var info by remember { mutableStateOf<LcdInfo?>(null) }

    LaunchedEffect(conn) {
        while (isActive) {
            info = try { conn?.lcdInfo() } catch (_: Exception) { null }
            delay(2000)
        }
    }

    SpiceBackHandler(enabled = fullscreen.value) { fullscreen.value = false }
    val uiMode = LocalUiMode.current
    val title = "LCD Info"

    if (uiMode == UiMode.Miuix) {
        top.yukonga.miuix.kmp.basic.Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    SmallTopAppBar(title = title,
                        navigationIcon = { top.yukonga.miuix.kmp.basic.IconButton(onClick = onBack) { top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null) } },
                        actions = { FullscreenAction() })
                }
            },
        ) { innerPadding ->
            LcdInfoContentMiuix(info, if (fullscreen.value) PaddingValues(0.dp) else innerPadding)
        }
    } else {
        Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    TopAppBar(title = { Text(title) },
                        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } },
                        actions = { FullscreenAction() })
                }
            },
        ) { innerPadding ->
            LcdInfoContent(info, if (fullscreen.value) PaddingValues(0.dp) else innerPadding)
        }
    }
}

@Composable
private fun LcdInfoContent(info: LcdInfo?, padding: PaddingValues) {
    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (info == null) { Text("No LCD data available"); return@Column }
        Text("Enabled: ${info.enabled}", style = MaterialTheme.typography.bodyLarge)
        Text("CSM: ${info.csm}")
        Text("Brightness: ${info.brightness}")
        Text("Contrast: ${info.contrast}")
        Text("Backlight: ${info.backlight}")
        Text("Red: ${info.red}  Green: ${info.green}  Blue: ${info.blue}")
    }
}

@Composable
private fun LcdInfoContentMiuix(info: LcdInfo?, padding: PaddingValues) {
    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (info == null) { top.yukonga.miuix.kmp.basic.Text("No LCD data available"); return@Column }
        top.yukonga.miuix.kmp.basic.Text("Enabled: ${info.enabled}")
        top.yukonga.miuix.kmp.basic.Text("CSM: ${info.csm}")
        top.yukonga.miuix.kmp.basic.Text("Brightness: ${info.brightness}")
        top.yukonga.miuix.kmp.basic.Text("Contrast: ${info.contrast}")
        top.yukonga.miuix.kmp.basic.Text("Backlight: ${info.backlight}")
        top.yukonga.miuix.kmp.basic.Text("Red: ${info.red}  Green: ${info.green}  Blue: ${info.blue}")
    }
}
