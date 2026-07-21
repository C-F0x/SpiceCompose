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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cf0x.spicecompose.data.ServerConfig
import org.cf0x.spicecompose.network.ConnectionStatus
import org.cf0x.spicecompose.platform.maybeVibrate
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.navigation.LocalWindowSize
import org.cf0x.spicecompose.ui.navigation.WindowSize
import org.cf0x.spicecompose.ui.screen.about.APP_VERSION
import org.cf0x.spicecompose.ui.theme.LocalStatusColors
import org.cf0x.spicecompose.ui.theme.isInDarkTheme
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun StatusHomeMiuix(
    connectionStatus: ConnectionStatus,
    currentServer: ServerConfig?,
    avsInfo: Map<String, String>,
    launcherInfo: Map<String, String>,
    memoryInfo: Map<String, Long>,
    onServerAction: (Boolean) -> Unit,
    onStatusClick: () -> Unit,
    onEditServer: () -> Unit
) {
    val scrollBehavior = MiuixScrollBehavior()
    val strings = LocalAppStrings.current
    val isConnected = connectionStatus == ConnectionStatus.Connected
    val isConnecting = connectionStatus == ConnectionStatus.Connecting
    val isMonet = MiuixTheme.isDynamicColor
    val statusColors = LocalStatusColors.current
    val windowSize = LocalWindowSize.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = strings.status,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding()),
        ) {
            item { Spacer(Modifier.height(12.dp)) }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    // Left Large Card: Connection Status
                    Card(
                        modifier = Modifier.weight(1.3f).fillMaxHeight().padding(end = 12.dp),
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
                                else -> colorScheme.surfaceVariant
                            }
                        ),
                        onClick = { maybeVibrate(15); onStatusClick() },
                        pressFeedbackType = PressFeedbackType.Tilt
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Icon(
                                modifier = Modifier.size(170.dp).align(Alignment.BottomEnd).offset(38.dp, 45.dp),
                                imageVector = when {
                                    isConnected -> Icons.Rounded.CheckCircleOutline
                                    isConnecting -> Icons.Rounded.Refresh
                                    else -> Icons.Rounded.ErrorOutline
                                },
                                tint = when {
                                    isConnected -> if (isMonet) colorScheme.primary.copy(alpha = 0.8f) else statusColors.connected
                                    isConnecting -> colorScheme.primary.copy(alpha = 0.8f)
                                    else -> colorScheme.onSurfaceVariantActions.copy(alpha = 0.2f)
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
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold
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
                            }
                        }
                    }

                    // Right column
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f).padding(bottom = 12.dp),
                            insideMargin = PaddingValues(16.dp),
                            onClick = { maybeVibrate(15); onServerAction(true) },
                            onLongPress = { maybeVibrate(15); onEditServer() },
                            showIndication = true,
                            pressFeedbackType = PressFeedbackType.Tilt
                        ) {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                                Text(strings.targetServer, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = colorScheme.onSurfaceVariantSummary)
                                Text(currentServer?.name ?: "None", fontSize = 26.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            insideMargin = PaddingValues(16.dp),
                            pressFeedbackType = PressFeedbackType.Tilt
                        ) {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                                Text(strings.version, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = colorScheme.onSurfaceVariantSummary)
                                Text(APP_VERSION, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                val cols = if (windowSize == WindowSize.Compact) 1 else 2
                Column {
                    CardItemMiuix(
                        title = strings.avsInfo,
                        content = if (isConnected) "${avsInfo["model"] ?: ""}-${avsInfo["dest"] ?: ""}.${avsInfo["spec"] ?: ""}.${avsInfo["rev"] ?: ""}-${avsInfo["ext"] ?: ""}"
                                  else "model-dest.spec.rev-ext"
                    )
                    
                    Row(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Box(Modifier.weight(1f).padding(end = 12.dp)) {
                             CardItemMiuix(title = strings.backendUrl, content = avsInfo["services"] ?: "...")
                        }
                        if (cols > 1) {
                             Box(Modifier.weight(1f)) {
                                 CardItemMiuix(title = strings.spiceCompile, content = if (isConnected) "${launcherInfo["compile_date"] ?: ""} ${launcherInfo["compile_time"] ?: ""}" else "...")
                             }
                        }
                    }
                    if (cols == 1) {
                        Box(Modifier.padding(top = 12.dp)) {
                            CardItemMiuix(title = strings.spiceCompile, content = if (isConnected) "${launcherInfo["compile_date"] ?: ""} ${launcherInfo["compile_time"] ?: ""}" else "...")
                        }
                    }

                    Box(Modifier.padding(top = 12.dp)) {
                        MemoryStackedCardMiuix(strings.memoryStacked, memoryInfo, isConnected)
                    }
                    
                    Row(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Box(Modifier.weight(1f).padding(end = 12.dp)) {
                            CardItemMiuix(strings.spiceVersion, launcherInfo["version"] ?: "...")
                        }
                        Box(Modifier.weight(1f)) {
                            CardItemMiuix(strings.systemTime, launcherInfo["system_time"] ?: "...")
                        }
                    }
                    
                    Box(Modifier.padding(top = 12.dp)) {
                        CardItemMiuix(strings.launcherArgs, formatArgs(launcherInfo["args"]))
                    }
                }
            }

            // ── Bottom spacing ───────────────────────────────────────────────
            item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
        }
    }
}

@Composable
fun CardItemMiuix(title: String, content: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 14.sp, color = colorScheme.onSurfaceVariantSummary)
            Text(content, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun MemoryStackedCardMiuix(title: String, memory: Map<String, Long>, isConnected: Boolean) {
    val gameUsed = if (isConnected) (memory["mem_used"] ?: 0L) else 0L
    val totalUsed = if (isConnected) (memory["mem_total_used"] ?: 1L) else 0L
    val total = if (isConnected) (memory["mem_total"] ?: 1L) else 1L
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
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
        }
    }
}
