package org.cf0x.spicecompose.ui.screen.utils.subscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.getRoundedCorner

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
    var savedToast by remember { mutableStateOf(false) }

    val cm = LocalConnectionManager.current
    // Dialog overall height cap (90% of the window). The scrollable content column uses
    // weight(fill=false), so Compose decides automatically whether scrolling is needed:
    // short content keeps its natural height, tall content fills the free space and scrolls.
    val dialogMaxHeight = with(LocalDensity.current) {
        val h = LocalWindowInfo.current.containerSize.height.toFloat()
        if (h > 0f) (h * 0.9f).toDp() else 600.dp
    }
    LaunchedEffect(cm.getClient()) {
        val conn = cm.getClient() ?: return@LaunchedEffect
        availableScreens = try { conn.captureGetScreens() } catch (_: Exception) { emptyList() }
    }

    SpiceBackHandler(enabled = fullscreen.value) { fullscreen.value = false }

    if (showSettings) {
        // local state copies — only committed on OK
        var localQuality by remember { mutableIntStateOf(p.ssQuality) }
        var localDivide by remember { mutableIntStateOf(p.ssDivide) }
        var localThreads by remember { mutableIntStateOf(p.ssThreads) }
        var localPollInterval by remember { mutableIntStateOf(p.ssPollIntervalMs) }
        var localScreen by remember { mutableIntStateOf(captureScreen) }

        Dialog(
            onDismissRequest = { showSettings = false; savedToast = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .heightIn(max = dialogMaxHeight)
                    .squircleSurface(color = colorScheme.background, cornerRadius = getRoundedCorner())
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    strings.screenshotSettings,
                    color = colorScheme.onBackground,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                // Scrollable settings area — scrolls automatically when content
                // exceeds the free space (weight fill=false keeps natural height)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Capture2x mode is temporarily disabled — Original only.
                    Text("${strings.quality}: ${localQuality}%"); Spacer(Modifier.height(4.dp))
                    Slider(value = localQuality.toFloat(), onValueChange = { localQuality = it.toInt() }, valueRange = 10f..100f)
                    Spacer(Modifier.height(12.dp))
                    Text("${strings.divide}: ${localDivide}"); Spacer(Modifier.height(4.dp))
                    Slider(value = localDivide.toFloat(), onValueChange = { localDivide = it.toInt() }, valueRange = 1f..16f)
                    Spacer(Modifier.height(12.dp))
                    Text("${strings.threads}: ${localThreads}"); Spacer(Modifier.height(4.dp))
                    Slider(value = localThreads.toFloat(), onValueChange = { localThreads = it.toInt() }, valueRange = 1f..16f)
                    Text(strings.threadsHint, color = colorScheme.onSurfaceVariantSummary)
                    Spacer(Modifier.height(12.dp))
                    Text("${strings.pollInterval}: ${localPollInterval}"); Spacer(Modifier.height(4.dp))
                    Slider(value = localPollInterval.toFloat(), onValueChange = { localPollInterval = it.toInt() }, valueRange = 0f..500f)
                    Text(strings.pollIntervalHint, color = colorScheme.onSurfaceVariantSummary)
                    if (availableScreens.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("${strings.screen}: $localScreen / ${availableScreens.max()}")
                        Spacer(Modifier.height(4.dp))
                        Slider(value = localScreen.toFloat(), onValueChange = { localScreen = it.toInt() }, valueRange = availableScreens.min().toFloat()..availableScreens.max().toFloat(), steps = availableScreens.size - 2)
                    }

                    Spacer(Modifier.height(16.dp))
                    Box(Modifier.fillMaxWidth().height(0.5.dp).background(colorScheme.onSurfaceVariantSummary.copy(alpha = 0.2f)))
                    Spacer(Modifier.height(8.dp))

                    // Share + Refresh buttons inside the dialog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Share / Save screenshot
                        top.yukonga.miuix.kmp.basic.Button(
                            onClick = {
                                latestCapture?.let {
                                    saveImage(it, "screenshot.jpg")
                                    savedToast = true
                                }
                            },
                            enabled = latestCapture != null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Share, contentDescription = null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(strings.share)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        top.yukonga.miuix.kmp.basic.Button(
                            onClick = { refreshTick++ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(strings.refresh)
                            }
                        }
                    }

                    if (savedToast) {
                        Spacer(Modifier.height(4.dp))
                        Text(strings.savedToDownloads, color = colorScheme.primary)
                    }
                    }

                    Spacer(Modifier.height(16.dp))
                    // OK — always visible outside the scroll area
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        TextButton(
                            text = strings.ok,
                            onClick = {
                                // batch-commit all params
                                p.updateSsQuality(localQuality)
                                p.updateSsDivide(localDivide)
                                p.updateSsThreads(localThreads)
                                p.updateSsPollIntervalMs(localPollInterval)
                                captureScreen = localScreen
                                refreshTick++
                                showSettings = false
                                savedToast = false
                            },
                            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary()
                        )
                    }
                }
            }
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
            SubScreenContent(
                refreshTrigger = refreshTick,
                captureScreen = captureScreen,
                captureQuality = p.ssQuality,
                captureDivide = p.ssDivide,
                pollThreads = p.ssThreads,
                pollIntervalMs = p.ssPollIntervalMs.toLong(),
                captureMode = p.captureMode,
                capture2xFps = p.capture2xFps,
                capture2xDivide = p.capture2xDivide,
                onShareReady = { latestCapture = it }
            )
        }
    }
}
