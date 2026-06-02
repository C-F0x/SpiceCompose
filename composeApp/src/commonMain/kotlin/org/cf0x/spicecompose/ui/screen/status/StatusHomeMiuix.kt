package org.cf0x.spicecompose.ui.screen.status

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.ErrorOutline
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
import org.cf0x.spicecompose.ui.theme.isInDarkTheme
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles
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
    onServerClick: () -> Unit,
    onConnectLongClick: () -> Unit
) {
    val scrollBehavior = MiuixScrollBehavior()
    val strings = LocalAppStrings.current
    val isConnected = connectionStatus == ConnectionStatus.Connected

    Scaffold(
        topBar = {
            TopAppBar(
                title = strings.connected,
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
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = CardDefaults.defaultColors(
                            color = when {
                                isConnected -> if (isInDarkTheme()) Color(0xFF1A3825) else Color(0xFFDFFAE4)
                                else -> colorScheme.surfaceVariant
                            }
                        ),
                        pressFeedbackType = PressFeedbackType.Tilt
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Icon(
                                modifier = Modifier.size(150.dp).align(Alignment.BottomEnd).offset(30.dp, 40.dp),
                                imageVector = if (isConnected) Icons.Rounded.CheckCircleOutline else Icons.Rounded.ErrorOutline,
                                tint = if (isConnected) Color(0xFF36D167).copy(alpha = 0.5f) else colorScheme.onSurfaceVariantActions.copy(alpha = 0.2f),
                                contentDescription = null
                            )
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = if (isConnected) strings.connected else strings.disconnected,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (isConnected) {
                                    Text(
                                        text = launcherInfo["compile_date"] ?: "",
                                        fontSize = 12.sp,
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
                        // Target Server Card
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            onClick = onServerClick,
                            onLongPress = onConnectLongClick,
                            pressFeedbackType = PressFeedbackType.Tilt
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(strings.targetServer, fontSize = 14.sp, color = colorScheme.onSurfaceVariantSummary)
                                Text(currentServer?.name ?: "None", fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                        // Bonjour Card
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            pressFeedbackType = PressFeedbackType.Tilt
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(strings.bonjour, fontSize = 14.sp, color = colorScheme.onSurfaceVariantSummary)
                                Text("SpiceCompose", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (isConnected) {
                item {
                    Column(Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // AVS Info item
                        CardItemMiuix(
                            title = "AVS Info",
                            content = "${avsInfo["model"] ?: ""}-${avsInfo["dest"] ?: ""}.${avsInfo["spec"] ?: ""}.${avsInfo["rev"] ?: ""}-${avsInfo["ext"] ?: ""}"
                        )
                        // Spice Compile Info
                        CardItemMiuix(
                            title = strings.spiceCompile,
                            content = "${launcherInfo["compile_date"] ?: ""} ${launcherInfo["compile_time"] ?: ""}"
                        )
                        // Memory
                        MemoryStackedCardMiuix(strings.memoryStacked, memoryInfo)
                        
                        // Launcher info
                        CardItemMiuix(strings.spiceVersion, launcherInfo["version"] ?: "")
                        CardItemMiuix(strings.systemTime, launcherInfo["system_time"] ?: "")
                        CardItemMiuix(strings.launcherArgs, (launcherInfo["args"] ?: "").replace("[", "").replace("]", ""))
                    }
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
fun MemoryStackedCardMiuix(title: String, memory: Map<String, Long>) {
    val gameUsed = memory["mem_used"] ?: 0L
    val totalUsed = memory["mem_total_used"] ?: 1L
    val total = memory["mem_total"] ?: 1L
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 14.sp, color = colorScheme.onSurfaceVariantSummary)
            Spacer(Modifier.height(8.dp))
            // Stacked bar
            Box(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(colorScheme.surfaceVariant)) {
                // Total used
                Box(Modifier.fillMaxWidth(totalUsed.toFloat() / total).fillMaxHeight().background(colorScheme.primary.copy(alpha = 0.3f)))
                // Game used
                Box(Modifier.fillMaxWidth(gameUsed.toFloat() / total).fillMaxHeight().background(colorScheme.primary))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${gameUsed / 1024 / 1024}MB / ${totalUsed / 1024 / 1024}MB / ${total / 1024 / 1024}MB", fontSize = 12.sp)
            }
        }
    }
}
