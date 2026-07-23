package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import org.cf0x.spicecompose.ui.component.AdaptiveTopAppBar
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.theme.LocalEnableBlur
import org.cf0x.spicecompose.ui.theme.CustomPreferences
import org.cf0x.spicecompose.ui.util.BlurredBar
import org.cf0x.spicecompose.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import org.cf0x.spicecompose.ui.navigation.horizontalCutoutPadding
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LcdInfoScreen(onBack: () -> Unit) {
    val cm = LocalConnectionManager.current
    val conn = cm.getClient()
    val fullscreen = LocalFullscreenMode.current
    val p = CustomPreferences
    var info by remember { mutableStateOf<LcdInfo?>(null) }

    LaunchedEffect(conn) {
        while (isActive) {
            info = try { conn?.lcdInfo() } catch (_: Exception) { null }
            delay(2000)
        }
    }

    SpiceBackHandler(enabled = fullscreen.value) { fullscreen.value = false }
    val strings = LocalAppStrings.current
    val uiMode = LocalUiMode.current
    val title = strings.lcdInfo
    val currentInfo = info

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
                            color = barColor,
                            scrollBehavior = scrollBehavior)
                    }
                }
            },
        ) { innerPadding ->
            val topPadding = if (fullscreen.value) 0.dp else innerPadding.calculateTopPadding()
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .scrollEndHaptic()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = 12.dp)
                    .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
                contentPadding = PaddingValues(top = topPadding),
            ) {
                item { Spacer(Modifier.height(12.dp)) }
                if (currentInfo == null) {
                    item { top.yukonga.miuix.kmp.basic.Text(LocalAppStrings.current.noLcdData) }
                } else {
                    item {
                        top.yukonga.miuix.kmp.basic.Text("Enabled: ${currentInfo.enabled}")
                        top.yukonga.miuix.kmp.basic.Text("CSM: ${currentInfo.csm}")
                        top.yukonga.miuix.kmp.basic.Text("Brightness: ${currentInfo.brightness}")
                        top.yukonga.miuix.kmp.basic.Text("Contrast: ${currentInfo.contrast}")
                        top.yukonga.miuix.kmp.basic.Text("Backlight: ${currentInfo.backlight}")
                        top.yukonga.miuix.kmp.basic.Text("Red: ${currentInfo.red}  Green: ${currentInfo.green}  Blue: ${currentInfo.blue}")
                    }
                }
                item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
            }
        }
    } else {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    AdaptiveTopAppBar(title = { Text(title) },
                        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) } },
                        actions = { FullscreenAction() },
                        scrollBehavior = scrollBehavior)
                }
            },
        ) { innerPadding ->
            val topPadding = if (fullscreen.value) 0.dp else innerPadding.calculateTopPadding()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalCutoutPadding()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(top = topPadding),
            ) {
                item { Spacer(Modifier.height(12.dp)) }
                if (currentInfo == null) {
                    item { Text(LocalAppStrings.current.noLcdData) }
                } else {
                    item {
                        Text("Enabled: ${currentInfo.enabled}", style = MaterialTheme.typography.bodyLarge)
                        Text("CSM: ${currentInfo.csm}")
                        Text("Brightness: ${currentInfo.brightness}")
                        Text("Contrast: ${currentInfo.contrast}")
                        Text("Backlight: ${currentInfo.backlight}")
                        Text("Red: ${currentInfo.red}  Green: ${currentInfo.green}  Blue: ${currentInfo.blue}")
                    }
                }
                item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
            }
        }
    }
}

