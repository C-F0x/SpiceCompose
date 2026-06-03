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
import androidx.compose.ui.text.font.FontWeight
import org.cf0x.spicecompose.data.ServerConfig
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import androidx.compose.material.icons.rounded.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusPagerMaterial(
    servers: List<ServerConfig>,
    chosenId: String?,
    onSelect: (String?) -> Unit,
    onAddClick: () -> Unit,
    onDelete: (String) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val strings = LocalAppStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Servers") },
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
                    ServerCardMaterial(
                        server = server,
                        isChosen = server.id == chosenId,
                        onSelect = { onSelect(if (server.id == chosenId) null else server.id) },
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerCardMaterial(
    server: ServerConfig,
    isChosen: Boolean,
    onSelect: () -> Unit,
    onDelete: (String) -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        onClick = { showConfirm = true }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Computer,
                    contentDescription = null,
                    tint = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(16.dp))
                Text(server.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (isChosen) {
                    Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(8.dp))
            InfoLineMaterial(strings.serverHost, server.host)
            InfoLineMaterial(strings.serverPort, server.port.toString())
            InfoLineMaterial(strings.serverPassword, server.password)
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Confirm Action") },
            text = { Text("What would you like to do with \"${server.name}\"?") },
            confirmButton = {
                TextButton(onClick = { onSelect(); showConfirm = false }) {
                    Text(if (isChosen) "Unselect" else "Select")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { onDelete(server.id); showConfirm = false }) {
                        Text(strings.delete, color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { showConfirm = false }) {
                        Text(strings.cancel)
                    }
                }
            }
        )
    }
}

@Composable
fun InfoLineMaterial(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.width(100.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
