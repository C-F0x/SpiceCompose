package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.infoAVS
import org.cf0x.spicecompose.network.spiceapi.wrappers.infoLauncher
import org.cf0x.spicecompose.network.spiceapi.wrappers.infoMemory
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.component.AdaptiveTopAppBar
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.theme.LocalEnableBlur
import org.cf0x.spicecompose.ui.theme.CustomPreferences
import org.cf0x.spicecompose.ui.navigation.horizontalCutoutPadding
import org.cf0x.spicecompose.ui.util.BlurredBar
import org.cf0x.spicecompose.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun CabinetInfoScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val connectionManager = LocalConnectionManager.current
    val connection = connectionManager.getClient()
    val fullscreen = LocalFullscreenMode.current
    val p = CustomPreferences
    
    SpiceBackHandler(enabled = fullscreen.value) {
        fullscreen.value = false
    }

    var avsInfo by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var launcherInfo by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var memoryInfo by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }

    LaunchedEffect(connection) {
        if (connection == null) return@LaunchedEffect
        while (isActive) {
            try {
                // Fetch all info
                avsInfo = connection.infoAVS()
                launcherInfo = connection.infoLauncher().mapValues { it.value.toString().replace("\"", "") }
                memoryInfo = connection.infoMemory()
            } catch (e: Exception) {
                // ignore
            }
            delay(1000)
        }
    }

    when (LocalUiMode.current) {
        UiMode.Miuix -> {
            val scrollBehavior = MiuixScrollBehavior()
            val enableBlur = LocalEnableBlur.current
            val backdrop = rememberBlurBackdrop(enableBlur && LocalUiMode.current == UiMode.Miuix)
            val blurActive = backdrop != null
            val barColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surface
            top.yukonga.miuix.kmp.basic.Scaffold(
                topBar = {
                    if (!fullscreen.value && !p.toolbarHidden) {
                        BlurredBar(backdrop, blurActive) {
                            SmallTopAppBar(
                                title = strings.cabinetInfo,
                                navigationIcon = {
                                    top.yukonga.miuix.kmp.basic.IconButton(onClick = onBack) {
                                        top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null)
                                    }
                                },
                                actions = {
                                    FullscreenAction()
                                },
                                color = barColor,
                                scrollBehavior = scrollBehavior,
                            )
                        }
                    }
                }
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
                    item {
                        InfoSectionMiuix(strings.avsInfo, avsInfo)
                    }
                    item {
                        InfoSectionMiuix(strings.launcherInfo, launcherInfo)
                    }
                    item {
                        MemorySectionMiuix(memoryInfo)
                    }

                    // ── Bottom spacing ───────────────────────────────────────────────
                    item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
                }
            }
        }
        UiMode.Material -> {
            @OptIn(ExperimentalMaterial3Api::class)
            val scrollBehavior = androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior()
            androidx.compose.material3.Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    if (!fullscreen.value && !p.toolbarHidden) {
                        @OptIn(ExperimentalMaterial3Api::class)
                        AdaptiveTopAppBar(
                            title = { androidx.compose.material3.Text(strings.cabinetInfo) },
                            navigationIcon = {
                                androidx.compose.material3.IconButton(onClick = onBack) {
                                    androidx.compose.material3.Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                                }
                            },
                            actions = {
                                FullscreenAction()
                            },
                            scrollBehavior = scrollBehavior,
                        )
                    }
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
                    contentPadding = PaddingValues(top = if (fullscreen.value) 0.dp else innerPadding.calculateTopPadding())
                ) {
                    item { InfoSectionMaterial(strings.avsInfo, avsInfo) }
                    item { InfoSectionMaterial(strings.launcherInfo, launcherInfo) }
                    item { MemorySectionMaterial(memoryInfo) }
                    item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
                }
            }
        }
    }
}

@Composable
fun InfoSectionMiuix(title: String, data: Map<String, String>) {
    if (data.isEmpty()) return
    top.yukonga.miuix.kmp.basic.Text(
        title, 
        modifier = Modifier.padding(start = 28.dp, top = 16.dp, bottom = 8.dp),
        fontSize = 14.sp,
        color = MiuixTheme.colorScheme.primary
    )
    top.yukonga.miuix.kmp.basic.Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
    ) {
        data.forEach { (k, v) ->
            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                top.yukonga.miuix.kmp.basic.Text(k, Modifier.weight(1f), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                top.yukonga.miuix.kmp.basic.Text(v, color = MiuixTheme.colorScheme.onSurfaceVariantActions)
            }
        }
    }
}

@Composable
fun InfoSectionMaterial(title: String, data: Map<String, String>) {
    if (data.isEmpty()) return
    androidx.compose.material3.Text(
        title,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
    )
    data.forEach { (k, v) ->
        ListItem(
            headlineContent = { androidx.compose.material3.Text(k) },
            supportingContent = { androidx.compose.material3.Text(v) }
        )
    }
}

@Composable
fun MemorySectionMiuix(data: Map<String, Long>) {
    if (data.isEmpty()) return
    InfoSectionMiuix(LocalAppStrings.current.memoryUsage, data.mapValues { 
        if (it.key.contains("total") || it.key.contains("used")) "${it.value / 1024 / 1024} MB"
        else it.value.toString()
    })
}

@Composable
fun MemorySectionMaterial(data: Map<String, Long>) {
    if (data.isEmpty()) return
    InfoSectionMaterial(LocalAppStrings.current.memoryUsage, data.mapValues { 
        if (it.key.contains("total") || it.key.contains("used")) "${it.value / 1024 / 1024} MB"
        else it.value.toString()
    })
}
