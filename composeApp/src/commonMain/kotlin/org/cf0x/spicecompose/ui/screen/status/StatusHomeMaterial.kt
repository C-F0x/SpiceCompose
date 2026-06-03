package org.cf0x.spicecompose.ui.screen.status

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
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
import org.cf0x.spicecompose.ui.component.TonalCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusHomeMaterial(
    connectionStatus: ConnectionStatus,
    currentServer: ServerConfig?,
    avsInfo: Map<String, String>,
    launcherInfo: Map<String, String>,
    memoryInfo: Map<String, Long>,
    onServerAction: (Boolean) -> Unit,
    onStatusClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isConnected = connectionStatus == ConnectionStatus.Connected
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val windowSize = LocalWindowSize.current

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(strings.status) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card (Top Large)
            TonalCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = if (isConnected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
                onClick = onStatusClick
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                    Column(Modifier.padding(start = 20.dp)) {
                        Text(
                            text = if (isConnected) strings.connected else strings.disconnected,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (isConnected) {
                            Text(
                                text = launcherInfo["compile_date"] ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Row of 2 Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TonalCard(
                    modifier = Modifier.weight(1f),
                    onClick = { onServerAction(false) }, // Try Connect
                    onLongClick = { onServerAction(true) } // List
                ) {
                    Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                        Text(strings.targetServer, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                        Text(
                            currentServer?.name ?: "None",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                TonalCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                        Text(strings.bonjour, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                        Text(
                            "SpiceCompose",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Info items (Grid for large screen)
            val cols = if (windowSize == WindowSize.Compact) 1 else 2
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoItemMaterial(
                    title = "AVS Info",
                    content = if (isConnected) "${avsInfo["model"] ?: ""}-${avsInfo["dest"] ?: ""}.${avsInfo["spec"] ?: ""}.${avsInfo["rev"] ?: ""}-${avsInfo["ext"] ?: ""}"
                              else "model-dest.spec.rev-ext"
                )
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        InfoItemMaterial(title = strings.backendUrl, content = avsInfo["services"] ?: "...")
                    }
                    if (cols > 1) {
                        Box(Modifier.weight(1f)) {
                            InfoItemMaterial(title = strings.spiceCompile, content = if (isConnected) "${launcherInfo["compile_date"] ?: ""} ${launcherInfo["compile_time"] ?: ""}" else "...")
                        }
                    }
                }
                if (cols == 1) {
                    InfoItemMaterial(title = strings.spiceCompile, content = if (isConnected) "${launcherInfo["compile_date"] ?: ""} ${launcherInfo["compile_time"] ?: ""}" else "...")
                }

                MemoryStackedMaterial(strings.memoryStacked, memoryInfo, isConnected)
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        InfoItemMaterial(strings.spiceVersion, launcherInfo["version"] ?: "...")
                    }
                    Box(Modifier.weight(1f)) {
                        InfoItemMaterial(strings.systemTime, launcherInfo["system_time"] ?: "...")
                    }
                }

                InfoItemMaterial(strings.launcherArgs, (launcherInfo["args"] ?: "...").replace("[", "").replace("]", ""))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun InfoItemMaterial(title: String, content: String) {
    TonalCard {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun MemoryStackedMaterial(title: String, memory: Map<String, Long>, isConnected: Boolean) {
    val gameUsed = if (isConnected) (memory["mem_used"] ?: 0L) else 0L
    val totalUsed = if (isConnected) (memory["mem_total_used"] ?: 1L) else 0L
    val total = if (isConnected) (memory["mem_total"] ?: 1L) else 1L
    
    TonalCard {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                Box(Modifier.fillMaxWidth(if (total > 0) totalUsed.toFloat() / total else 0f).fillMaxHeight().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)))
                Box(Modifier.fillMaxWidth(if (total > 0) gameUsed.toFloat() / total else 0f).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
            }
            Spacer(Modifier.height(4.dp))
            Text("${gameUsed / 1024 / 1024}MB / ${totalUsed / 1024 / 1024}MB / ${total / 1024 / 1024}MB", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}
