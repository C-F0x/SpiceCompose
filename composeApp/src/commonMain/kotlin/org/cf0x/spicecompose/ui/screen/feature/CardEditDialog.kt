package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.data.CardConfig
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.util.CardCipher
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import org.cf0x.spicecompose.platform.NfcManager
import org.cf0x.spicecompose.platform.VibratorManager

@Composable
fun CardEditDialog(
    show: Boolean,
    card: CardConfig? = null,
    onSave: (CardConfig) -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(show) { mutableStateOf(card?.name ?: "") }
    var cardId by remember(show) { mutableStateOf(card?.cardId ?: "") }
    var publicId by remember(show) { mutableStateOf("") }
    var triggerId by remember(show) { mutableStateOf(card?.idTrigger ?: "") }

    val strings = LocalAppStrings.current

    // Listen for NFC during editing
    LaunchedEffect(show) {
        if (show) {
            NfcManager.tagIdFlow.collect { id ->
                cardId = id
                VibratorManager.vibrate(100)
            }
        }
    }

    // Update publicId when cardId changes
    LaunchedEffect(cardId) {
        if (cardId.length == 16) {
            try {
                val encoded = CardCipher.encode(cardId)
                if (encoded != publicId) publicId = encoded
            } catch (e: Exception) {
                publicId = ""
            }
        }
    }

    // Update cardId when publicId changes
    val onPublicIdChange: (String) -> Unit = { newVal ->
        val upper = newVal.uppercase()
        publicId = upper
        if (upper.length == 16) {
            try {
                val decoded = CardCipher.decode(upper)
                if (decoded.length == 16 && decoded != cardId) {
                    cardId = decoded
                }
            } catch (e: Exception) { }
        }
    }

    if (!show) return

    when (LocalUiMode.current) {
        UiMode.Miuix -> {
            OverlayDialog(
                show = show,
                title = if (card == null) strings.addCard else strings.editCard,
                onDismissRequest = onDismiss,
                content = {
                    Column(Modifier.fillMaxWidth()) {
                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            label = strings.cardName,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        TextField(
                            value = cardId,
                            onValueChange = { cardId = it.uppercase() },
                            label = strings.cardId,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        TextField(
                            value = publicId,
                            onValueChange = onPublicIdChange,
                            label = strings.publicId,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        TextField(
                            value = triggerId,
                            onValueChange = { triggerId = it.uppercase() },
                            label = strings.triggerId,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                text = strings.discard,
                                onClick = onDiscard,
                                modifier = Modifier.weight(1f),
                                colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary()
                            )
                            TextButton(
                                text = strings.save,
                                onClick = {
                                    if (name.isEmpty() || cardId.length != 16) return@TextButton
                                    val newCard = CardConfig(
                                        id = card?.id ?: (1..16).map { "0123456789ABCDEF".random() }.joinToString(""),
                                        name = name,
                                        cardId = cardId,
                                        idTrigger = triggerId,
                                        active = card?.active ?: false
                                    )
                                    onSave(newCard)
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
                onDismissRequest = onDismiss,
                title = { Text(if (card == null) strings.addCard else strings.editCard) },
                text = {
                    Column(Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(strings.cardName) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cardId,
                            onValueChange = { cardId = it.uppercase() },
                            label = { Text(strings.cardId) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = publicId,
                            onValueChange = onPublicIdChange,
                            label = { Text(strings.publicId) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = triggerId,
                            onValueChange = { triggerId = it.uppercase() },
                            label = { Text(strings.triggerId) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    androidx.compose.material3.Button(onClick = {
                        if (name.isEmpty() || cardId.length != 16) return@Button
                        val newCard = CardConfig(
                            id = card?.id ?: (1..16).map { "0123456789ABCDEF".random() }.joinToString(""),
                            name = name,
                            cardId = cardId,
                            idTrigger = triggerId,
                            active = card?.active ?: false
                        )
                        onSave(newCard)
                    }) {
                        Text(strings.save)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = onDiscard,
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = androidx.compose.ui.graphics.Color.Red)
                    ) {
                        Text(strings.discard)
                    }
                }
            )
        }
    }
}
