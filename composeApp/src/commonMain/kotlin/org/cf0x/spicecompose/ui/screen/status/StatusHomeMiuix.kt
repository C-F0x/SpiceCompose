package org.cf0x.spicecompose.ui.screen.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.squircle.squircleBackground
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cf0x.spicecompose.data.ServerConfig
import org.cf0x.spicecompose.network.ConnectionStatus
import org.cf0x.spicecompose.platform.maybeVibrate
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.navigation.LocalWindowSize
import org.cf0x.spicecompose.ui.navigation.WindowSize
import org.cf0x.spicecompose.util.APP_VERSION
import org.cf0x.spicecompose.ui.theme.LocalStatusColors
import org.cf0x.spicecompose.ui.theme.isInDarkTheme
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.theme.LocalEnableBlur
import org.cf0x.spicecompose.ui.util.BlurredBar
import org.cf0x.spicecompose.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun StatusHomeMiuix(
    connectionStatus: ConnectionStatus,
    currentServer: ServerConfig?,
    latencyMs: Long,
    avsInfo: Map<String, String>,
    launcherInfo: Map<String, String>,
    memoryInfo: Map<String, Long>,
    onServerAction: (Boolean) -> Unit,
    onStatusClick: () -> Unit,
    onEditServer: () -> Unit
) {
    val scrollBehavior = MiuixScrollBehavior()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur && LocalUiMode.current == UiMode.Miuix)
    val blurActive = backdrop != null
    val barColor = if (blurActive) Color.Transparent else colorScheme.surface
    val strings = LocalAppStrings.current
    val isConnected = connectionStatus == ConnectionStatus.Connected
    val isConnecting = connectionStatus == ConnectionStatus.Connecting
    val isMonet = MiuixTheme.isDynamicColor
    val statusColors = LocalStatusColors.current
    val windowSize = LocalWindowSize.current
    val cols = if (windowSize == WindowSize.Compact) 1 else 2

    Scaffold(
        topBar = {
            BlurredBar(backdrop, blurActive) {
                TopAppBar(
                    title = strings.status,
                    color = barColor,
                    scrollBehavior = scrollBehavior
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding()),
        ) {
            // ── Top spacing ─────────────────────────────────────────────────
            item { Spacer(Modifier.height(12.dp)) }

            // ── Top 2×2 block: Status + Server + Version ────────────────────
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp)
                ) {
                    // Left: connection status card (spans 2 rows)
                    Box(Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                            colors = CardDefaults.defaultColors(
                                color = when {
                                    isConnected -> {
                                        if (isMonet) colorScheme.secondaryContainer
                                        else if (isInDarkTheme()) statusColors.connected.copy(alpha = 0.15f)
                                        else statusColors.connected.copy(alpha = 0.1f)
                                    }
                                    isConnecting -> {
                                        if (isMonet) colorScheme.primaryContainer
                                        else statusColors.connecting.copy(alpha = 0.1f)
                                    }
                                    else -> {
                                        if (isMonet) colorScheme.errorContainer
                                        else if (isInDarkTheme()) statusColors.danger.copy(alpha = 0.15f)
                                        else statusColors.danger.copy(alpha = 0.1f)
                                    }
                                }
                            ),
                            onClick = { maybeVibrate(15); onStatusClick() },
                            pressFeedbackType = PressFeedbackType.Tilt
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    modifier = Modifier.size(120.dp).align(Alignment.BottomEnd).offset(28.dp, 35.dp),
                                    imageVector = when {
                                        isConnected -> Icons.Rounded.CheckCircleOutline
                                        isConnecting -> Icons.Rounded.Refresh
                                        else -> Icons.Rounded.ErrorOutline
                                    },
                                    tint = when {
                                        isConnected -> if (isMonet) colorScheme.primary.copy(alpha = 0.8f) else statusColors.connected
                                        isConnecting -> colorScheme.primary.copy(alpha = 0.8f)
                                        else -> if (isMonet) colorScheme.error.copy(alpha = 0.8f) else statusColors.danger
                                    },
                                    contentDescription = null
                                )
                                Column(Modifier.padding(all = 16.dp)) {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = when {
                                            isConnected -> strings.connected
                                            isConnecting -> strings.connecting
                                            else -> strings.disconnected
                                        },
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (!isConnected && !isConnecting) {
                                            if (isMonet) colorScheme.error else statusColors.danger
                                        } else Color.Unspecified
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = when {
                                            isConnected -> strings.disconnectHint
                                            isConnecting -> ""
                                            else -> strings.connectHint
                                        },
                                        fontSize = 13.sp,
                                        color = colorScheme.onSurfaceVariantSummary
                                    )
                                    if (isConnected) {
                                        Text(
                                            modifier = Modifier.fillMaxWidth(),
                                            text = "${strings.latency} : ${if (latencyMs > 0) "${latencyMs} ms" else "--"}",
                                            fontSize = 12.sp,
                                            color = colorScheme.onSurfaceVariantSummary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.width(12.dp))
                    // Right column: server + version stacked
                    Column(Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            insideMargin = PaddingValues(16.dp),
                            onClick = { maybeVibrate(15); onServerAction(true) },
                            onLongPress = { maybeVibrate(15); onEditServer() },
                            showIndication = true,
                            pressFeedbackType = PressFeedbackType.Tilt
                        ) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                                Text(strings.targetServer, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = colorScheme.onSurfaceVariantSummary)
                                Text(currentServer?.name ?: "None", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            insideMargin = PaddingValues(16.dp),
                            pressFeedbackType = PressFeedbackType.Tilt
                        ) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                                Text(strings.version, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = colorScheme.onSurfaceVariantSummary)
                                Text(APP_VERSION, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // ── AVS Info Card ───────────────────────────────────────────────
            item {
                CardItemMiuix(
                    modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp),
                    title = strings.avsInfo,
                    content = if (isConnected) "${avsInfo["model"] ?: ""}-${avsInfo["dest"] ?: ""}.${avsInfo["spec"] ?: ""}.${avsInfo["rev"] ?: ""}-${avsInfo["ext"] ?: ""}"
                              else "model-dest.spec.rev-ext"
                )
            }

            // ── Backend URL (+ Spice Compile side-by-side) ──────────────────
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp)
                ) {
                    Box(Modifier.weight(1f)) {
                        CardItemMiuix(title = strings.backendUrl, content = avsInfo["services"] ?: "...")
                    }
                    if (cols > 1) {
                        Spacer(Modifier.width(12.dp))
                        Box(Modifier.weight(1f)) {
                            CardItemMiuix(
                                title = strings.spiceCompile,
                                content = if (isConnected) "${launcherInfo["compile_date"] ?: ""} ${launcherInfo["compile_time"] ?: ""}" else "..."
                            )
                        }
                    }
                }
            }
            if (cols == 1) {
                item {
                    CardItemMiuix(
                        modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp),
                        title = strings.spiceCompile,
                        content = if (isConnected) "${launcherInfo["compile_date"] ?: ""} ${launcherInfo["compile_time"] ?: ""}" else "..."
                    )
                }
            }

            // ── Memory Stacked Card ─────────────────────────────────────────
            item {
                MemoryStackedCardMiuix(
                    modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp),
                    title = strings.memoryStacked,
                    memory = memoryInfo,
                    isConnected = isConnected
                )
            }

            // ── Spice Version + System Time ─────────────────────────────────
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp)
                ) {
                    Box(Modifier.weight(1f)) {
                        CardItemMiuix(title = strings.spiceVersion, content = launcherInfo["version"] ?: "...")
                    }
                    Spacer(Modifier.width(12.dp))
                    Box(Modifier.weight(1f)) {
                        CardItemMiuix(title = strings.systemTime, content = launcherInfo["system_time"] ?: "...")
                    }
                }
            }

            // ── Launcher Args Card ──────────────────────────────────────────
            item {
                CardItemMiuix(
                    modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp),
                    title = strings.launcherArgs,
                    content = formatArgs(launcherInfo["args"])
                )
            }

            // ── Bottom spacing ──────────────────────────────────────────────
            item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
        }
    }
}

@Composable
fun CardItemMiuix(modifier: Modifier = Modifier, title: String, content: String) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 14.sp, color = colorScheme.onSurfaceVariantSummary)
            Text(content, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun MemoryStackedCardMiuix(modifier: Modifier = Modifier, title: String, memory: Map<String, Long>, isConnected: Boolean) {
    val gameUsed = if (isConnected) (memory["mem_used"] ?: 0L) else 0L
    val totalUsed = if (isConnected) (memory["mem_total_used"] ?: 1L) else 0L
    val total = if (isConnected) (memory["mem_total"] ?: 1L) else 1L
    val vmemUsed = if (isConnected) (memory["vmem_used"] ?: 0L) else 0L
    val vmemTotalUsed = if (isConnected) (memory["vmem_total_used"] ?: 0L) else 0L
    val vmemTotal = if (isConnected) (memory["vmem_total"] ?: 0L) else 0L
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            // ── Physical Memory bar ──
            Text(title, fontSize = 14.sp, color = colorScheme.onSurfaceVariantSummary)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(12.dp).squircleBackground(color = colorScheme.surfaceVariant, cornerRadius = 6.dp)) {
                Box(Modifier.fillMaxWidth(if (total > 0) totalUsed.toFloat() / total else 0f).fillMaxHeight().background(colorScheme.primary.copy(alpha = 0.3f)))
                Box(Modifier.fillMaxWidth(if (total > 0) gameUsed.toFloat() / total else 0f).fillMaxHeight().background(colorScheme.primary))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${gameUsed / 1024 / 1024}MB / ${totalUsed / 1024 / 1024}MB / ${total / 1024 / 1024}MB", fontSize = 12.sp)
            }
            // ── Virtual Memory bar ──
            if (vmemTotal > 0) {
                Spacer(Modifier.height(12.dp))
                Text("Virtual", fontSize = 14.sp, color = colorScheme.onSurfaceVariantSummary)
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(12.dp).squircleBackground(color = colorScheme.surfaceVariant, cornerRadius = 6.dp)) {
                    Box(Modifier.fillMaxWidth(if (vmemTotal > 0) vmemTotalUsed.toFloat() / vmemTotal else 0f).fillMaxHeight().background(colorScheme.primary.copy(alpha = 0.3f)))
                    Box(Modifier.fillMaxWidth(if (vmemTotal > 0) vmemUsed.toFloat() / vmemTotal else 0f).fillMaxHeight().background(colorScheme.primary))
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${vmemUsed / 1024 / 1024}MB / ${vmemTotalUsed / 1024 / 1024}MB / ${vmemTotal / 1024 / 1024}MB", fontSize = 12.sp)
                }
            }
        }
    }
}
