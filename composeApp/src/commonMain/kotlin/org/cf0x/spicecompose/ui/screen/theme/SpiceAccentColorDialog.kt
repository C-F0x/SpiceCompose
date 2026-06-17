package org.cf0x.spicecompose.ui.screen.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.ui.component.ColorPickerWheel
import org.cf0x.spicecompose.ui.theme.rememberSystemAccentColor

/**
 * Accent color dialog using ColorPickerWheel.
 * Bottom bar: [系统取色] [取消] [确认]
 */
@Composable
fun SpiceAccentColorDialog(
    current: Color,
    onConfirm: (Color) -> Unit,
    onDismiss: () -> Unit,
) {
    val systemAccent = rememberSystemAccentColor()
    var pickedColor by remember { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("强调色") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ColorPickerWheel(
                    initialColor = current,
                    onColorChanged = { pickedColor = it },
                )
                Spacer(Modifier.height(12.dp))
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // 系统取色
                OutlinedButton(
                    onClick = { onConfirm(systemAccent) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("系统取色", maxLines = 1)
                }
                // 取消
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("取消")
                }
                // 确认
                Button(
                    onClick = { onConfirm(pickedColor) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("确认")
                }
            }
        },
    )
}
