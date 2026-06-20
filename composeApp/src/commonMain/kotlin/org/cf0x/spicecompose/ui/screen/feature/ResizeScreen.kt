package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.imageResizeEnable
import org.cf0x.spicecompose.network.spiceapi.wrappers.imageResizeSetScene
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
fun ResizeScreen(onBack: () -> Unit) {
    val cm = LocalConnectionManager.current
    val conn = cm.getClient()
    val scope = rememberCoroutineScope()
    val fullscreen = LocalFullscreenMode.current
    val p = ThemePreferences
    var enabled by remember { mutableStateOf(false) }
    var scene by remember { mutableIntStateOf(0) }

    SpiceBackHandler(enabled = fullscreen.value) { fullscreen.value = false }

    val uiMode = LocalUiMode.current
    val title = "Screen Resize"

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
            val pad = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
            Column(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    top.yukonga.miuix.kmp.basic.Text("Enable Resize", Modifier.weight(1f))
                    Switch(enabled, onCheckedChange = { enabled = it; scope.launch { conn?.imageResizeEnable(it) } })
                }
                top.yukonga.miuix.kmp.basic.Text("Scene: $scene")
                Slider(value = scene.toFloat(), onValueChange = { scene = it.toInt(); scope.launch { conn?.imageResizeSetScene(scene) } }, valueRange = 0f..10f, steps = 9)
            }
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
            val pad = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
            Column(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable Resize", Modifier.weight(1f))
                    Switch(enabled, onCheckedChange = { enabled = it; scope.launch { conn?.imageResizeEnable(it) } })
                }
                Text("Scene: $scene")
                Slider(value = scene.toFloat(), onValueChange = { scene = it.toInt(); scope.launch { conn?.imageResizeSetScene(scene) } }, valueRange = 0f..10f, steps = 9)
            }
        }
    }
}
