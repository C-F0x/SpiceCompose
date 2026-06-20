package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.ButtonState
import org.cf0x.spicecompose.network.spiceapi.wrappers.buttonsRead
import org.cf0x.spicecompose.network.spiceapi.wrappers.buttonsWrite
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.platform.maybeVibrate
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.theme.ThemePreferences
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ButtonsScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val fullscreen = LocalFullscreenMode.current
    val p = ThemePreferences
    
    val connectionManager = LocalConnectionManager.current
    val connection = connectionManager.getClient()
    val scope = rememberCoroutineScope()
    
    var buttonStates by remember { mutableStateOf<List<ButtonState>>(emptyList()) }

    SpiceBackHandler(enabled = fullscreen.value) {
        fullscreen.value = false
    }

    LaunchedEffect(connection) {
        if (connection == null) {
            buttonStates = emptyList()
            return@LaunchedEffect
        }
        while (isActive) {
            try { buttonStates = connection.buttonsRead() } catch (_: Exception) { }
            delay(200)
        }
    }

    val onPress: (ButtonState) -> Unit = { button ->
        maybeVibrate(50)
        scope.launch {
            connection?.buttonsWrite(listOf(button.copy(state = 1.0, active = true)))
            delay(100)
            connection?.buttonsWrite(listOf(button.copy(state = 0.0, active = true)))
        }
    }

    val uiMode = LocalUiMode.current

    if (uiMode == UiMode.Miuix) {
        top.yukonga.miuix.kmp.basic.Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    SmallTopAppBar(
                        title = strings.buttons,
                        navigationIcon = { IconButton(onClick = onBack) { top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null) } },
                        actions = { FullscreenAction() }
                    )
                }
            }
        ) { innerPadding ->
            val padding = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
            if (buttonStates.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    top.yukonga.miuix.kmp.basic.Text("No buttons available :(")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(buttonStates) { button ->
                        ButtonMiuix(button, onPress)
                    }
                }
            }
        }
    } else {
        androidx.compose.material3.Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    @OptIn(ExperimentalMaterial3Api::class)
                    androidx.compose.material3.TopAppBar(
                        title = { androidx.compose.material3.Text(strings.buttons) },
                        navigationIcon = {
                            androidx.compose.material3.IconButton(onClick = onBack) {
                                androidx.compose.material3.Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                            }
                        },
                        actions = { FullscreenAction() }
                    )
                }
            }
        ) { innerPadding ->
            val padding = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
            if (buttonStates.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Text("No buttons available :(")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(buttonStates) { button ->
                        ButtonMaterial(button, onPress)
                    }
                }
            }
        }
    }
}

@Composable
private fun ButtonMiuix(button: ButtonState, onPress: (ButtonState) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onPress(button) },
        pressFeedbackType = top.yukonga.miuix.kmp.utils.PressFeedbackType.Sink,
        showIndication = true
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = button.name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ButtonMaterial(button: ButtonState, onPress: (ButtonState) -> Unit) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onPress(button) },
        shape = androidx.compose.material3.MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text(
                text = button.name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
