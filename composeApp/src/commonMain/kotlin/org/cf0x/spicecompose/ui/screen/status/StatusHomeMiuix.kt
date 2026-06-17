package org.cf0x.spicecompose.ui.screen.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cf0x.spicecompose.data.ServerConfig
import org.cf0x.spicecompose.network.ConnectionStatus
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.navigation.LocalWindowSize
import org.cf0x.spicecompose.ui.navigation.WindowSize
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
    onStatusClick: () -> Unit
) {
    val scrollBehavior = MiuixScrollBehavior()
    val strings = LocalAppStrings.current
    val isConnected = connectionStatus == ConnectionStatus.Connected
    val isConnecting = connectionStatus == ConnectionStatus.Connecting
    val isMonet = MiuixTheme.isDynamicColor
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
            contentPadding = innerPadding
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Large Card: Connection Status
                    Card(
                        modifier = Modifier.weight(1.3f).fillMaxHeight(),
                        colors = CardDefaults.defaultColors(
                            color = when {
                                isConnected -> {
                                    if (isMonet) colorScheme.secondaryContainer
                                    else if (isInDarkTheme()) Color(0xFF1A3825)
                                    else Color(0xFFDFFAE4)
                                }
                                isConnecting -> {
                                    if (isMonet) colorScheme.primaryContainer
                                    else Color(0xFFFFF4E5)
                                }
                                else -> colorScheme.surfaceVariant
                            }
                        ),
                        onClick = onStatusClick,
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
                                    isConnected -> if (isMonet) colorScheme.primary.copy(alpha = 0.8f) else Color(0xFF36D167)
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
                                        isConnecting -> "Connecting..."
                                        else -> strings.disconnected
                                    },
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(2.dp))
                                if (isConnected) {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = launcherInfo["compile_date"] ?: "",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colorScheme.onSurfaceVariantSummary
                                    )
                                }
                            }
                        }
                    }

                    // Right column
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            insideMargin = PaddingValues(16.dp),
                            onClick = { onServerAction(true) }, // Single tap: Server List
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
                                Text(strings.bonjour, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = colorScheme.onSurfaceVariantSummary)
                                Text("SpiceCompose", fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                val cols = if (windowSize == WindowSize.Compact) 1 else 2
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CardItemMiuix(
                        title = "AVS Info",
                        content = if (isConnected) "${avsInfo["model"] ?: ""}-${avsInfo["dest"] ?: ""}.${avsInfo["spec"] ?: ""}.${avsInfo["rev"] ?: ""}-${avsInfo["ext"] ?: ""}"
                                  else "model-dest.spec.rev-ext"
                    )
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) {
                             CardItemMiuix(title = strings.backendUrl, content = avsInfo["services"] ?: "...")
                        }
                        if (cols > 1) {
                             Box(Modifier.weight(1f)) {
                                 CardItemMiuix(title = strings.spiceCompile, content = if (isConnected) "${launcherInfo["compile_date"] ?: ""} ${launcherInfo["compile_time"] ?: ""}" else "...")
                             }
                        }
                    }
                    if (cols == 1) {
                        CardItemMiuix(title = strings.spiceCompile, content = if (isConnected) "${launcherInfo["compile_date"] ?: ""} ${launcherInfo["compile_time"] ?: ""}" else "...")
                    }

                    MemoryStackedCardMiuix(strings.memoryStacked, memoryInfo, isConnected)
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) {
                            CardItemMiuix(strings.spiceVersion, launcherInfo["version"] ?: "...")
                        }
                        Box(Modifier.weight(1f)) {
                            CardItemMiuix(strings.systemTime, launcherInfo["system_time"] ?: "...")
                        }
                    }
                    
                    CardItemMiuix(strings.launcherArgs, (launcherInfo["args"] ?: "...").replace("[", "").replace("]", ""))
                }
            }
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
            Box(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(colorScheme.surfaceVariant)) {
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
