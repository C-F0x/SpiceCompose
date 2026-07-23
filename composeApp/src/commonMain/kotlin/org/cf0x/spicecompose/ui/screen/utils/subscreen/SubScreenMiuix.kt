package org.cf0x.spicecompose.ui.screen.utils.subscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.captureGetScreens
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.theme.CustomPreferences
import org.cf0x.spicecompose.platform.saveImage
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.overlay.OverlayDialog

@Composable
fun SubScreenMiuix(
    onBack: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val scrollBehavior = MiuixScrollBehavior()
    val fullscreen = LocalFullscreenMode.current
    var refreshTick by remember { mutableStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    var latestCapture by remember { mutableStateOf<ByteArray?>(null) }
    var captureScreen by remember { mutableIntStateOf(1) }
    var availableScreens by remember { mutableStateOf<List<Int>>(emptyList()) }
    val p = CustomPreferences

    val cm = LocalConnectionManager.current
    LaunchedEffect(cm.getClient()) {
        val conn = cm.getClient() ?: return@LaunchedEffect
        availableScreens = try { conn.captureGetScreens() } catch (_: Exception) { emptyList() }
    }

    SpiceBackHandler(enabled = fullscreen.value) { fullscreen.value = false }

    if (showSettings) {
        OverlayDialog(
            show = showSettings,
            title = strings.screenshotSettings,
            onDismissRequest = { showSettings = false },
            content = {
                Column(Modifier.fillMaxWidth()) {
                    Text("${strings.quality}: ${p.ssQuality}%"); Spacer(Modifier.height(4.dp))
                    Slider(value = p.ssQuality.toFloat(), onValueChange = { p.updateSsQuality(it.toInt()) }, valueRange = 10f..100f)
                    Spacer(Modifier.height(12.dp))
                    Text("${strings.divide}: ${p.ssDivide}"); Spacer(Modifier.height(4.dp))
                    Slider(value = p.ssDivide.toFloat(), onValueChange = { p.updateSsDivide(it.toInt()) }, valueRange = 1f..16f)
                    if (availableScreens.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Screen: $captureScreen / ${availableScreens.max()}")
                        Spacer(Modifier.height(4.dp))
                        Slider(value = captureScreen.toFloat(), onValueChange = { captureScreen = it.toInt() }, valueRange = availableScreens.min().toFloat()..availableScreens.max().toFloat(), steps = availableScreens.size - 2)
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        TextButton(
                            text = strings.ok,
                            onClick = { showSettings = false },
                            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary()
                        )
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (!fullscreen.value && !p.toolbarHidden) {
                SmallTopAppBar(
                    title = strings.subScreen,
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(MiuixIcons.Back, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = strings.screenshotSettings)
                        }
                        IconButton(onClick = { latestCapture?.let { saveImage(it, "screenshot.jpg") } }) {
                            Icon(Icons.Rounded.Share, contentDescription = strings.share)
                        }
                        IconButton(onClick = { refreshTick++ }) {
                            Icon(Icons.Rounded.Refresh, contentDescription = strings.refresh)
                        }
                        FullscreenAction()
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
    ) { innerPadding ->
        val padding = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            SubScreenContent(refreshTrigger = refreshTick, captureScreen = captureScreen, captureQuality = p.ssQuality, captureDivide = p.ssDivide, onShareReady = { latestCapture = it })
        }
    }
}
