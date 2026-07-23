package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import org.cf0x.spicecompose.ui.component.AdaptiveTopAppBar
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.theme.ThemePreferences
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import org.cf0x.spicecompose.ui.theme.LocalEnableBlur
import org.cf0x.spicecompose.ui.util.BlurredBar
import org.cf0x.spicecompose.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

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

    val strings = LocalAppStrings.current
    val uiMode = LocalUiMode.current
    val title = strings.screenResize

    if (uiMode == UiMode.Miuix) {
        val scrollBehavior = MiuixScrollBehavior()
        val enableBlur = LocalEnableBlur.current
        val backdrop = rememberBlurBackdrop(enableBlur && LocalUiMode.current == UiMode.Miuix)
        val blurActive = backdrop != null
        val barColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surface
        top.yukonga.miuix.kmp.basic.Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    BlurredBar(backdrop, blurActive) {
                        SmallTopAppBar(title = title,
                            navigationIcon = { top.yukonga.miuix.kmp.basic.IconButton(onClick = onBack) { top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null) } },
                            actions = { FullscreenAction() },
                            color = barColor)
                    }
                }
            },
        ) { innerPadding ->
            val pad = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
            Column(Modifier.fillMaxSize().padding(pad).padding(16.dp).then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    top.yukonga.miuix.kmp.basic.Text(strings.enableResize, Modifier.weight(1f))
                    top.yukonga.miuix.kmp.basic.Switch(enabled, onCheckedChange = { enabled = it; scope.launch { conn?.imageResizeEnable(it) } })
                }
                top.yukonga.miuix.kmp.basic.Text(strings.sceneLabel.replace("%d", scene.toString()))
                top.yukonga.miuix.kmp.basic.Slider(value = scene.toFloat(), onValueChange = { scene = it.toInt() }, onValueChangeFinished = { scope.launch { conn?.imageResizeSetScene(scene) } }, valueRange = 0f..10f, steps = 9)
            }
        }
    } else {
        Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    AdaptiveTopAppBar(title = { Text(title) },
                        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } },
                        actions = { FullscreenAction() })
                }
            },
        ) { innerPadding ->
            val pad = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
            Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.enableResize, Modifier.weight(1f))
                    Switch(enabled, onCheckedChange = { enabled = it; scope.launch { conn?.imageResizeEnable(it) } })
                }
                Text(strings.sceneLabel.replace("%d", scene.toString()))
                Slider(value = scene.toFloat(), onValueChange = { scene = it.toInt() }, onValueChangeFinished = { scope.launch { conn?.imageResizeSetScene(scene) } }, valueRange = 0f..10f, steps = 9)
            }
        }
    }
}
