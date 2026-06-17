package org.cf0x.spicecompose.ui.screen.status

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import org.cf0x.spicecompose.data.ServerConfig
import org.cf0x.spicecompose.data.ServerRepository
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.infoAVS
import org.cf0x.spicecompose.network.spiceapi.wrappers.infoLauncher
import org.cf0x.spicecompose.network.spiceapi.wrappers.infoMemory
import org.cf0x.spicecompose.ui.LocalInSubPage
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
    var chosenId by remember { mutableStateOf(repository.chosenServerId) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showServerList by remember { mutableStateOf(false) }
    
    val connectionManager = LocalConnectionManager.current
    val mainState = LocalMainPagerState.current
    val status by connectionManager.status.collectAsState()
    val currentServer by connectionManager.currentServer.collectAsState()

    val chosenServer = remember(chosenId, servers) { servers.find { it.id == chosenId } }

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

    // Disable main pager swipe when server list sub-page is open
    val inSubPage = LocalInSubPage.current
    SideEffect { inSubPage.value = showServerList }

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

    val onStatusBlockClick = {
        if (status == ConnectionStatus.Connected) {
            connectionManager.disconnect()
        } else {
            chosenServer?.let { connectionManager.connect(it) } ?: run { showServerList = true }
        }
    }
    
    val onServerAction: (Boolean) -> Unit = { isLong ->
        if (isLong) {
            showServerList = true
        } else {
            chosenServer?.let { connectionManager.connect(it) } ?: run { showServerList = true }
        }
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = showServerList,
            transitionSpec = {
                if (targetState) {
                    (fadeIn(tween(400)) + expandVertically(tween(400), expandFrom = Alignment.Top))
                        .togetherWith(fadeOut(tween(400)) + shrinkVertically(tween(400), shrinkTowards = Alignment.Top))
                } else {
                    (fadeIn(tween(400)) + expandVertically(tween(400), expandFrom = Alignment.Top))
                        .togetherWith(fadeOut(tween(400)) + shrinkVertically(tween(400), shrinkTowards = Alignment.Top))
                }.using(SizeTransform(clip = false))
            },
            label = "ServerListTransition"
        ) { isList ->
            if (isList) {
                Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
                    SpiceBackHandler(enabled = true) { showServerList = false }

                    when (LocalUiMode.current) {
                        UiMode.Miuix -> StatusPagerMiuix(
                            servers = servers,
                            chosenId = chosenId,
                            onSelect = { id ->
                                repository.chosenServerId = id
                                chosenId = id
                                servers = repository.getServers() // Refresh UI
                            },
                            onAddClick = { showAddDialog = true },
                            onDelete = { id ->
                                repository.deleteServer(id)
                                servers = repository.getServers()
                                chosenId = repository.chosenServerId
                            }
                        )
                        UiMode.Material -> StatusPagerMaterial(
                            servers = servers,
                            chosenId = chosenId,
                            onSelect = { id ->
                                repository.chosenServerId = id
                                chosenId = id
                                servers = repository.getServers()
                            },
                            onAddClick = { showAddDialog = true },
                            onDelete = { id ->
                                repository.deleteServer(id)
                                servers = repository.getServers()
                                chosenId = repository.chosenServerId
                            }
                        )
                    }
                }
            } else {
                when (LocalUiMode.current) {
                    UiMode.Miuix -> StatusHomeMiuix(
                        connectionStatus = status,
                        currentServer = chosenServer,
                        avsInfo = avsInfo,
                        launcherInfo = launcherInfo,
                        memoryInfo = memoryInfo,
                        onServerAction = onServerAction,
                        onStatusClick = onStatusBlockClick
                    )
                    UiMode.Material -> StatusHomeMaterial(
                        connectionStatus = status,
                        currentServer = chosenServer,
                        avsInfo = avsInfo,
                        launcherInfo = launcherInfo,
                        memoryInfo = memoryInfo,
                        onServerAction = onServerAction,
                        onStatusClick = onStatusBlockClick
                    )
                }
            }
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
    }
}
