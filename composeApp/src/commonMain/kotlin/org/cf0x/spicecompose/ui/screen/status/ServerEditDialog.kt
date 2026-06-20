package org.cf0x.spicecompose.ui.screen.status

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.data.ServerConfig
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog

@Composable
fun ServerEditDialog(
    show: Boolean,
    server: ServerConfig? = null,
    onSave: (ServerConfig) -> Unit,
    onDelete: (() -> Unit)? = null,
    onCancel: () -> Unit
) {
    var name by remember(show) { mutableStateOf(server?.name ?: "") }
    var host by remember(show) { mutableStateOf(server?.host ?: "") }
    var port by remember(show) { mutableStateOf(server?.port?.toString() ?: "673") }
    var password by remember(show) { mutableStateOf(server?.password ?: "") }

    val strings = LocalAppStrings.current

    if (!show) return

    val isEditing = server != null

    when (LocalUiMode.current) {
        UiMode.Miuix -> {
            OverlayDialog(
                show = show,
                title = if (!isEditing) strings.addServer else "Edit Server",
                onDismissRequest = onCancel,
                content = {
                    Column(Modifier.fillMaxWidth()) {
                        TextField(value = name, onValueChange = { name = it }, label = strings.serverName, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                        TextField(value = host, onValueChange = { host = it }, label = strings.serverHost, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                        TextField(value = port, onValueChange = { port = it }, label = strings.serverPort, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                        TextField(value = password, onValueChange = { password = it }, label = strings.serverPassword, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (isEditing && onDelete != null) {
                                TextButton(
                                    text = strings.delete,
                                    onClick = onDelete,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.textButtonColorsPrimary()
                                )
                            }
                            TextButton(
                                text = strings.cancel,
                                onClick = onCancel,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                text = strings.save,
                                onClick = {
                                    val p = port.toIntOrNull() ?: 673
                                    val randomHex = (1..16).map { "0123456789ABCDEF".random() }.joinToString("")
                                    val newServer = ServerConfig(
                                        id = server?.id ?: randomHex,
                                        name = name, host = host, port = p, password = password
                                    )
                                    onSave(newServer)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColorsPrimary()
                            )
                        }
                    }
                }
            )
        }
        UiMode.Material -> {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = onCancel,
                title = { Text(if (!isEditing) strings.addServer else "Edit Server") },
                text = {
                    Column(Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(strings.serverName) }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = host, onValueChange = { host = it }, label = { Text(strings.serverHost) }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = port, onValueChange = { port = it }, label = { Text(strings.serverPort) }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text(strings.serverPassword) }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    androidx.compose.material3.Button(onClick = {
                        val p = port.toIntOrNull() ?: 673
                        val randomHex = (1..16).map { "0123456789ABCDEF".random() }.joinToString("")
                        val newServer = ServerConfig(id = server?.id ?: randomHex, name = name, host = host, port = p, password = password)
                        onSave(newServer)
                    }) { Text(strings.save) }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isEditing && onDelete != null) {
                            androidx.compose.material3.TextButton(
                                onClick = onDelete,
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = androidx.compose.ui.graphics.Color.Red)
                            ) { Text(strings.delete) }
                        }
                        androidx.compose.material3.TextButton(onClick = onCancel) { Text(strings.cancel) }
                    }
                }
            )
        }
    }
}
