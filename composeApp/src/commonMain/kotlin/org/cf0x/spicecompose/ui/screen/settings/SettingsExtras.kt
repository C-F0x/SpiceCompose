package org.cf0x.spicecompose.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.controlRestart
import org.cf0x.spicecompose.network.spiceapi.wrappers.controlShutdown
import org.cf0x.spicecompose.network.spiceapi.wrappers.controlReboot
import org.cf0x.spicecompose.platform.maybeVibrate
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.preference.ArrowPreference

// ── Game Control Buttons ──

@Composable
fun GameControlSection() {
    val strings = LocalAppStrings.current
    val connectionManager = LocalConnectionManager.current
    val scope = rememberCoroutineScope()
    val client = connectionManager.getClient()

    if (LocalUiMode.current == UiMode.Miuix) {
        top.yukonga.miuix.kmp.basic.Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Column(Modifier.padding(12.dp)) {
                top.yukonga.miuix.kmp.basic.Text("Game Control", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    top.yukonga.miuix.kmp.basic.TextButton(
                        text = "Restart", onClick = { scope.launch { client?.controlRestart() } },
                        modifier = Modifier.weight(1f), enabled = client != null
                    )
                    top.yukonga.miuix.kmp.basic.TextButton(
                        text = "Shutdown", onClick = { scope.launch { client?.controlShutdown() } },
                        modifier = Modifier.weight(1f), enabled = client != null,
                        colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary()
                    )
                    top.yukonga.miuix.kmp.basic.TextButton(
                        text = "Reboot", onClick = { scope.launch { client?.controlReboot() } },
                        modifier = Modifier.weight(1f), enabled = client != null
                    )
                }
            }
        }
    } else {
        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Game Control", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { scope.launch { client?.controlRestart() } }, modifier = Modifier.weight(1f), enabled = client != null) { Text("Restart") }
                    Button(onClick = { scope.launch { client?.controlShutdown() } }, modifier = Modifier.weight(1f), enabled = client != null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Shutdown") }
                    OutlinedButton(onClick = { scope.launch { client?.controlReboot() } }, modifier = Modifier.weight(1f), enabled = client != null) { Text("Reboot") }
                }
            }
        }
    }
}

// ── Screenshot Parameter Section ──

@Composable
fun ScreenshotParamsSection(
    quality: Int, onQualityChange: (Int) -> Unit,
    divide: Int, onDivideChange: (Int) -> Unit
) {
    if (LocalUiMode.current == UiMode.Miuix) {
        top.yukonga.miuix.kmp.basic.Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Column(Modifier.padding(12.dp)) {
                top.yukonga.miuix.kmp.basic.Text("Screenshot", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                ArrowPreference(title = "Quality", summary = "${quality}%",
                    startAction = { top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.Image, null) })
                StepRowMiuix(quality, 10..100 step 10, onQualityChange)
                ArrowPreference(title = "Divide", summary = "$divide",
                    startAction = { top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.GridOn, null) })
                StepRowMiuix(divide, 1..16, onDivideChange)
            }
        }
    } else {
        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Screenshot", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Text("Quality: ${quality}%")
                Slider(value = quality.toFloat(), onValueChange = { onQualityChange(it.toInt()) }, valueRange = 10f..100f)
                Spacer(Modifier.height(8.dp))
                Text("Divide: $divide")
                Slider(value = divide.toFloat(), onValueChange = { onDivideChange(it.toInt()) }, valueRange = 1f..16f)
            }
        }
    }
}

@Composable
private fun StepRowMiuix(value: Int, range: IntProgression, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        range.forEach { v ->
            top.yukonga.miuix.kmp.basic.TextButton(
                text = "$v", onClick = { onChange(v) },
                colors = if (v == value) top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary()
                         else top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary()
            )
        }
    }
}

// ── Vibration Duration Section ──

@Composable
fun VibrationSection(durationMs: Int, onDurationChange: (Int) -> Unit) {
    val scope = rememberCoroutineScope()
    if (LocalUiMode.current == UiMode.Miuix) {
        top.yukonga.miuix.kmp.basic.Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Column(Modifier.padding(12.dp)) {
                top.yukonga.miuix.kmp.basic.Text("Vibration", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                ArrowPreference(title = "Duration", summary = "${durationMs}ms",
                    startAction = { top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.Vibration, null) })
                StepRowMiuix(durationMs, 0..200 step 10, onDurationChange)
                top.yukonga.miuix.kmp.basic.TextButton(
                    text = "Test", onClick = { scope.launch { maybeVibrate(durationMs.toLong()) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Vibration", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Text("Duration: ${durationMs}ms")
                Slider(value = durationMs.toFloat(), onValueChange = { onDurationChange(it.toInt()) }, valueRange = 0f..200f, steps = 19)
                TextButton(onClick = { scope.launch { maybeVibrate(durationMs.toLong()) } }) { Text("Test") }
            }
        }
    }
}
