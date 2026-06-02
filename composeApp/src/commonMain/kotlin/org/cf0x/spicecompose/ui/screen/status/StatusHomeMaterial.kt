package org.cf0x.spicecompose.ui.screen.status

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cf0x.spicecompose.data.ServerConfig
import org.cf0x.spicecompose.network.ConnectionStatus
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings

@Composable
fun StatusHomeMaterial(
    connectionStatus: ConnectionStatus,
    currentServer: ServerConfig?,
    avsInfo: Map<String, String>,
    launcherInfo: Map<String, String>,
    memoryInfo: Map<String, Long>,
    onServerClick: () -> Unit,
    onConnectLongClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isConnected = connectionStatus == ConnectionStatus.Connected

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Status") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = innerPadding
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Large Card
                    ElevatedCard(
                        modifier = Modifier.weight(1.3f).fillMaxHeight(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isConnected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Icon(
                                modifier = Modifier.size(160.dp).align(Alignment.BottomEnd).offset(30.dp, 40.dp),
                                imageVector = if (isConnected) Icons.Rounded.CheckCircleOutline else Icons.Rounded.ErrorOutline,
                                tint = if (isConnected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                                contentDescription = null
                            )
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = if (isConnected) strings.connected else strings.disconnected,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isConnected) {
                                    Text(
                                        text = launcherInfo["compile_date"] ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
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
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            onClick = onServerClick
                        ) {
                            Box(Modifier.fillMaxSize().combinedClickable(
                                onClick = onServerClick,
                                onLongClick = onConnectLongClick
                            )) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(strings.targetServer, style = MaterialTheme.typography.labelSmall)
                                    Text(currentServer?.name ?: "None", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(strings.bonjour, style = MaterialTheme.typography.labelSmall)
                                Text("SpiceCompose", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (isConnected) {
                item {
                    Column(Modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoItemMaterial(
                            title = "AVS Info",
                            content = "${avsInfo["model"] ?: ""}-${avsInfo["dest"] ?: ""}.${avsInfo["spec"] ?: ""}.${avsInfo["rev"] ?: ""}-${avsInfo["ext"] ?: ""}"
                        )
                        InfoItemMaterial(
                            title = strings.spiceCompile,
                            content = "${launcherInfo["compile_date"] ?: ""} ${launcherInfo["compile_time"] ?: ""}"
                        )
                        MemoryStackedMaterial(strings.memoryStacked, memoryInfo)
                        
                        InfoItemMaterial(strings.spiceVersion, launcherInfo["version"] ?: "")
                        InfoItemMaterial(strings.systemTime, launcherInfo["system_time"] ?: "")
                        InfoItemMaterial(strings.launcherArgs, (launcherInfo["args"] ?: "").replace("[", "").replace("]", ""))
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItemMaterial(title: String, content: String) {
    ListItem(
        headlineContent = { Text(content) },
        overlineContent = { Text(title) }
    )
}

@Composable
fun MemoryStackedMaterial(title: String, memory: Map<String, Long>) {
    val gameUsed = memory["mem_used"] ?: 0L
    val totalUsed = memory["mem_total_used"] ?: 1L
    val total = memory["mem_total"] ?: 1L
    
    ListItem(
        overlineContent = { Text(title) },
        headlineContent = {
            Column {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                    Box(Modifier.fillMaxWidth(totalUsed.toFloat() / total).fillMaxHeight().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)))
                    Box(Modifier.fillMaxWidth(gameUsed.toFloat() / total).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                }
                Spacer(Modifier.height(4.dp))
                Text("${gameUsed / 1024 / 1024}MB / ${totalUsed / 1024 / 1024}MB / ${total / 1024 / 1024}MB", style = MaterialTheme.typography.labelSmall)
            }
        }
    )
}
