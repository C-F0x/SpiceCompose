package org.cf0x.spicecompose.ui.screen.utils.subscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.theme.ThemePreferences
import org.cf0x.spicecompose.platform.saveImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScreenMaterial(
    onBack: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val fullscreen = LocalFullscreenMode.current
    var refreshTick by remember { mutableStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    var latestCapture by remember { mutableStateOf<ByteArray?>(null) }
    val p = ThemePreferences

    SpiceBackHandler(enabled = fullscreen.value) { fullscreen.value = false }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Screenshot Settings") },
            text = {
                Column {
                    Text("Quality: ${p.ssQuality}%"); Spacer(Modifier.height(4.dp))
                    Slider(value = p.ssQuality.toFloat(), onValueChange = { p.updateSsQuality(it.toInt()) }, valueRange = 10f..100f)
                    Spacer(Modifier.height(12.dp))
                    Text("Divide: ${p.ssDivide}"); Spacer(Modifier.height(4.dp))
                    Slider(value = p.ssDivide.toFloat(), onValueChange = { p.updateSsDivide(it.toInt()) }, valueRange = 1f..16f)
                }
            },
            confirmButton = { TextButton(onClick = { showSettings = false }) { Text("OK") } }
        )
    }

    Scaffold(
        topBar = {
            if (!fullscreen.value && !p.toolbarHidden) {
                TopAppBar(
                    title = { Text(strings.subScreen) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSettings = true }) { Icon(Icons.Rounded.MoreVert, contentDescription = "Settings") }
                        IconButton(onClick = { latestCapture?.let { saveImage(it, "screenshot.jpg") } }) {
                            Icon(Icons.Rounded.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = { refreshTick++ }) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                        }
                        FullscreenAction()
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
    ) { innerPadding ->
        val padding = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            SubScreenContent(refreshTrigger = refreshTick, onShareReady = { latestCapture = it })
        }
    }
}
