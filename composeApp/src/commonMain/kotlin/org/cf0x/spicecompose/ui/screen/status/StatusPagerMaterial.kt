package org.cf0x.spicecompose.ui.screen.status

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.data.ServerConfig
import org.cf0x.spicecompose.network.ConnectionStatus
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusPagerMaterial(
    servers: List<ServerConfig>,
    connectionStatus: ConnectionStatus,
    currentServer: ServerConfig?,
    onConnect: (ServerConfig) -> Unit,
    onDisconnect: () -> Unit,
    onAddClick: () -> Unit,
    onDelete: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Status") },
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
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = innerPadding,
            ) {
                items(servers, key = { it.id }) { server ->
                    var isExpanded by remember { mutableStateOf(false) }
                    val isActive = currentServer?.id == server.id && connectionStatus == ConnectionStatus.Connected
                    val isConnecting = currentServer?.id == server.id && connectionStatus == ConnectionStatus.Connecting
                    
                    ServerCardMaterial(
                        server = server,
                        isExpanded = isExpanded,
                        isActive = isActive,
                        isConnecting = isConnecting,
                        onExpandClick = { isExpanded = !isExpanded },
                        onConnectClick = { onConnect(server) },
                        onDisconnectClick = onDisconnect,
                        onDeleteConfirmed = { onDelete(server.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ServerCardMaterial(
    server: ServerConfig,
    isExpanded: Boolean,
    isActive: Boolean,
    isConnecting: Boolean,
    onExpandClick: () -> Unit,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val glowAlpha by animateFloatAsState(
        targetValue = if (isActive || isExpanded) 1f else 0f,
        animationSpec = tween(300),
        label = "glow"
    )
    val primary = MaterialTheme.colorScheme.primary

    val borderModifier = if (glowAlpha > 0f) {
        Modifier.border(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    primary.copy(alpha = glowAlpha),
                    primary.copy(alpha = glowAlpha * 0.3f),
                    primary.copy(alpha = glowAlpha)
                )
            ),
            shape = MaterialTheme.shapes.medium
        )
    } else Modifier

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .then(borderModifier)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        onClick = onExpandClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = if (isActive)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isConnecting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = if (isActive) Icons.Filled.Computer
                                else Icons.Outlined.Computer,
                                contentDescription = null,
                                tint = if (isActive)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = server.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (isActive) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text("ON", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(thickness = 0.5.dp)
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = strings.serverHost,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Text(
                        text = "${server.host}:${server.port}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDeleteConfirmed,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(strings.delete)
                        }

                        Spacer(Modifier.weight(1f))

                        Button(
                            onClick = { if (isActive) onDisconnectClick() else onConnectClick() },
                            colors = if (isActive)
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            else ButtonDefaults.buttonColors()
                        ) {
                            Text(if (isActive) strings.disconnect else if (isConnecting) "Connecting..." else strings.connect)
                        }
                    }
                }
            }
        }
    }
}
