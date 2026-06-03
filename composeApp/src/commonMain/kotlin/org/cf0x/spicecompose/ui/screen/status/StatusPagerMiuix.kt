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
import androidx.compose.ui.text.font.FontWeight
import org.cf0x.spicecompose.data.ServerConfig
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import androidx.compose.material.icons.rounded.Check

@Composable
fun StatusPagerMiuix(
    servers: List<ServerConfig>,
    chosenId: String?,
    onSelect: (String?) -> Unit,
    onAddClick: () -> Unit,
    onDelete: (String) -> Unit
) {
    val scrollBehavior = MiuixScrollBehavior()
    val strings = LocalAppStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Servers",
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
                    ServerCardMiuix(
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

@Composable
fun ServerCardMiuix(
    server: ServerConfig,
    isChosen: Boolean,
    onSelect: () -> Unit,
    onDelete: (String) -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true,
        onClick = { showConfirm = true }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Computer,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (isChosen) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(16.dp))
                Text(server.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (isChosen) {
                    Icon(Icons.Rounded.Check, null, tint = MiuixTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(12.dp))
            // Always show all 4 fields
            InfoLineMiuix(strings.serverHost, server.host)
            InfoLineMiuix(strings.serverPort, server.port.toString())
            InfoLineMiuix(strings.serverPassword, server.password)
        }
    }

    if (showConfirm) {
        OverlayDialog(
            show = showConfirm,
            onDismissRequest = { showConfirm = false },
            title = "Confirm Action",
            content = {
                Column {
                    TextButton(
                        text = if (isChosen) "Unselect" else "Select",
                        onClick = { onSelect(); showConfirm = false },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        text = strings.delete,
                        onClick = { onDelete(server.id); showConfirm = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                    TextButton(
                        text = strings.cancel,
                        onClick = { showConfirm = false },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

@Composable
fun InfoLineMiuix(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, modifier = Modifier.width(80.dp))
        Text(value, fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
}
