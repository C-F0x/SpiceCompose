package org.cf0x.spicecompose.ui.screen.status

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cf0x.spicecompose.data.ServerConfig
import org.cf0x.spicecompose.network.ConnectionStatus
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun StatusPagerMiuix(
    servers: List<ServerConfig>,
    connectionStatus: ConnectionStatus,
    currentServer: ServerConfig?,
    onConnect: (ServerConfig) -> Unit,
    onDisconnect: () -> Unit,
    onAddClick: () -> Unit,
    onDelete: (String) -> Unit
) {
    val scrollBehavior = MiuixScrollBehavior()
    val strings = LocalAppStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Status",
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                    }
                }
            )
        },
    ) { innerPadding ->
        if (servers.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(strings.noServers)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .scrollEndHaptic()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = 12.dp),
                contentPadding = innerPadding,
            ) {
                items(servers, key = { it.id }) { server ->
                    val isActive = (currentServer?.id == server.id) && (connectionStatus == ConnectionStatus.Connected)
                    val isConnecting = (currentServer?.id == server.id) && (connectionStatus == ConnectionStatus.Connecting)
                    
                    ServerCardMiuix(
                        server = server, 
                        isActive = isActive,
                        isConnecting = isConnecting,
                        onConnect = { onConnect(server) },
                        onDisconnect = onDisconnect,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}

@Composable
fun ServerCardMiuix(
    server: ServerConfig,
    isActive: Boolean,
    isConnecting: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onDelete: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true,
        onClick = { if (isActive) onDisconnect() else onConnect() },
        onLongPress = { showDeleteDialog = true }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Computer,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isActive) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(server.name, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                val statusText = when {
                    isConnecting -> "Connecting..."
                    isActive -> "Connected"
                    else -> "${server.host}:${server.port}"
                }
                Text(statusText, fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurface)
            }
        }
    }

    if (showDeleteDialog) {
        OverlayDialog(
            show = showDeleteDialog,
            title = strings.delete,
            onDismissRequest = { showDeleteDialog = false },
            content = {
                Text("Delete \"${server.name}\"?")
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(
                        text = strings.cancel,
                        onClick = { showDeleteDialog = false },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(20.dp))
                    TextButton(
                        text = strings.delete,
                        onClick = {
                            onDelete(server.id)
                            showDeleteDialog = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }
        )
    }
}
