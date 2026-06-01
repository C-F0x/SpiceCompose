package org.cf0x.spicecompose.ui.screen.status

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.filter
import org.cf0x.spicecompose.data.ServerConfig
import org.cf0x.spicecompose.data.ServerRepository
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.network.ConnectionStatus
import org.cf0x.spicecompose.ui.navigation.Destination
import org.cf0x.spicecompose.ui.navigation.LocalMainPagerState

@Composable
fun StatusScreen() {
    val repository = remember { ServerRepository() }
    var servers by remember { mutableStateOf(repository.getServers()) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    val connectionManager = LocalConnectionManager.current
    val mainState = LocalMainPagerState.current
    val status by connectionManager.status.collectAsState()
    val currentServer by connectionManager.currentServer.collectAsState()

    // Handle reset events from BottomBar
    LaunchedEffect(mainState) {
        mainState.resetEvents
            .filter { it == Destination.Status.index }
            .collect { showAddDialog = false }
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

    when (LocalUiMode.current) {
        UiMode.Miuix -> StatusPagerMiuix(
            servers = servers,
            connectionStatus = status,
            currentServer = currentServer,
            onConnect = { connectionManager.connect(it) },
            onDisconnect = { connectionManager.disconnect() },
            onAddClick = { showAddDialog = true },
            onDelete = { id ->
                repository.deleteServer(id)
                servers = repository.getServers()
            }
        )
        UiMode.Material -> StatusPagerMaterial(
            servers = servers,
            connectionStatus = status,
            currentServer = currentServer,
            onConnect = { connectionManager.connect(it) },
            onDisconnect = { connectionManager.disconnect() },
            onAddClick = { showAddDialog = true },
            onDelete = { id ->
                repository.deleteServer(id)
                servers = repository.getServers()
            }
        )
    }
}
