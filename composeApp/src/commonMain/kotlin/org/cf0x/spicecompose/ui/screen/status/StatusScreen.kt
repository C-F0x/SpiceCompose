package org.cf0x.spicecompose.ui.screen.status

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import org.cf0x.spicecompose.data.ServerConfig
import org.cf0x.spicecompose.data.ServerRepository
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.infoAVS
import org.cf0x.spicecompose.network.spiceapi.wrappers.infoLauncher
import org.cf0x.spicecompose.network.spiceapi.wrappers.infoMemory
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.network.ConnectionStatus
import org.cf0x.spicecompose.ui.navigation.Destination
import org.cf0x.spicecompose.ui.navigation.LocalMainPagerState

@Composable
fun StatusScreen() {
    val repository = remember { ServerRepository() }
    var servers by remember { mutableStateOf(repository.getServers()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showServerList by remember { mutableStateOf(false) }
    
    val connectionManager = LocalConnectionManager.current
    val mainState = LocalMainPagerState.current
    val status by connectionManager.status.collectAsState()
    val currentServer by connectionManager.currentServer.collectAsState()

    var avsInfo by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var launcherInfo by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var memoryInfo by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }

    // Handle reset events from BottomBar
    LaunchedEffect(mainState) {
        mainState.resetEvents
            .filter { it == Destination.Status.index }
            .collect { 
                showAddDialog = false
                showServerList = false
            }
    }

    LaunchedEffect(status) {
        if (status == ConnectionStatus.Connected) {
            val connection = connectionManager.getConnection()
            if (connection != null) {
                while (true) {
                    try {
                        avsInfo = connection.infoAVS()
                        launcherInfo = connection.infoLauncher().mapValues { it.value.toString().replace("\"", "") }
                        memoryInfo = connection.infoMemory()
                    } catch (_: Exception) { }
                    delay(2000)
                }
            }
        } else {
            avsInfo = emptyMap()
            launcherInfo = emptyMap()
            memoryInfo = emptyMap()
        }
    }

    if (showServerList) {
        SpiceBackHandler(enabled = true) { showServerList = false }

        when (LocalUiMode.current) {
            UiMode.Miuix -> StatusPagerMiuix(
                servers = servers,
                connectionStatus = status,
                currentServer = currentServer,
                onConnect = { connectionManager.connect(it); showServerList = false },
                onDisconnect = { connectionManager.disconnect() },
                onAddClick = { showAddDialog = true },
                onDelete = { id -> repository.deleteServer(id); servers = repository.getServers() }
            )
            UiMode.Material -> StatusPagerMaterial(
                servers = servers,
                connectionStatus = status,
                currentServer = currentServer,
                onConnect = { connectionManager.connect(it); showServerList = false },
                onDisconnect = { connectionManager.disconnect() },
                onAddClick = { showAddDialog = true },
                onDelete = { id -> repository.deleteServer(id); servers = repository.getServers() }
            )
        }

        ServerEditDialog(
            show = showAddDialog,
            onSave = {
                repository.addServer(it)
                servers = repository.getServers()
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    } else {
        when (LocalUiMode.current) {
            UiMode.Miuix -> StatusHomeMiuix(
                connectionStatus = status,
                currentServer = currentServer,
                avsInfo = avsInfo,
                launcherInfo = launcherInfo,
                memoryInfo = memoryInfo,
                onServerClick = { showServerList = true },
                onConnectLongClick = { currentServer?.let { connectionManager.connect(it) } }
            )
            UiMode.Material -> StatusHomeMaterial(
                connectionStatus = status,
                currentServer = currentServer,
                avsInfo = avsInfo,
                launcherInfo = launcherInfo,
                memoryInfo = memoryInfo,
                onServerClick = { showServerList = true },
                onConnectLongClick = { currentServer?.let { connectionManager.connect(it) } }
            )
        }
    }
}
