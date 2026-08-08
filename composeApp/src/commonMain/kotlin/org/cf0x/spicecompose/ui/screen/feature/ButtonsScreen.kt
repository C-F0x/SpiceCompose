package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.ButtonState
import org.cf0x.spicecompose.network.spiceapi.wrappers.buttonsRead
import org.cf0x.spicecompose.network.spiceapi.wrappers.buttonsWrite
import org.cf0x.spicecompose.network.spiceapi.wrappers.buttonsWriteReset
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.platform.maybeVibrate
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.component.AdaptiveTopAppBar
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.navigation.horizontalCutoutPadding
import org.cf0x.spicecompose.ui.theme.LocalEnableBlur
import org.cf0x.spicecompose.ui.theme.CustomPreferences
import org.cf0x.spicecompose.ui.util.BlurredBar
import org.cf0x.spicecompose.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.squircle.squircleBackground
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ButtonsScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val fullscreen = LocalFullscreenMode.current
    val p = CustomPreferences
    
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
            delay(maxOf(128L, connectionManager.latencyMs.value))
        }
    }

    // ── Press-down → state=1.0 ─────────────────────────────────────────
    val onPressDown: (ButtonState) -> Unit = { button ->
        maybeVibrate(30)
        scope.launch {
            connection?.buttonsWrite(listOf(button.copy(state = 1.0, active = true)))
        }
    }

    // ── Press-up → state=0.0 ───────────────────────────────────────────
    val onPressUp: (ButtonState) -> Unit = { button ->
        scope.launch {
            connection?.buttonsWrite(listOf(button.copy(state = 0.0, active = true)))
        }
    }

    val uiMode = LocalUiMode.current

    if (uiMode == UiMode.Miuix) {
        val scrollBehavior = MiuixScrollBehavior()
        val enableBlur = LocalEnableBlur.current
        val backdrop = rememberBlurBackdrop(enableBlur && LocalUiMode.current == UiMode.Miuix)
        val blurActive = backdrop != null
        val barColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surface
        top.yukonga.miuix.kmp.basic.Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    BlurredBar(backdrop, blurActive) {
                        SmallTopAppBar(
                            title = strings.buttons,
                            navigationIcon = { IconButton(onClick = onBack) { top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null) } },
                            actions = {
                                IconButton(onClick = { scope.launch { connection?.buttonsWriteReset(emptyList()) } }) { Icon(Icons.Rounded.Refresh, contentDescription = null) }
                                FullscreenAction()
                            },
                            color = barColor,
                            scrollBehavior = scrollBehavior
                        )
                    }
                }
            }
        ) { innerPadding ->
            val topPadding = innerPadding.calculateTopPadding()
            if (buttonStates.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(top = topPadding), contentAlignment = Alignment.Center) {
                    top.yukonga.miuix.kmp.basic.Text(strings.noButtonsAvailable)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize().horizontalCutoutPadding().nestedScroll(scrollBehavior.nestedScrollConnection)
                        .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
                    contentPadding = PaddingValues(top = topPadding + 12.dp)
                ) {
                    items(buttonStates) { button ->
                        ButtonMiuix(button, onPressDown, onPressUp)
                    }
                    item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
                }
            }
        }
    } else {
        @OptIn(ExperimentalMaterial3Api::class)
        val scrollBehavior = androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior()
        androidx.compose.material3.Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    @OptIn(ExperimentalMaterial3Api::class)
                    AdaptiveTopAppBar(
                        title = { androidx.compose.material3.Text(strings.buttons) },
                        navigationIcon = {
                            androidx.compose.material3.IconButton(onClick = onBack) {
                                androidx.compose.material3.Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                            }
                        },
                        actions = {
                                IconButton(onClick = { scope.launch { connection?.buttonsWriteReset(emptyList()) } }) { Icon(Icons.Rounded.Refresh, contentDescription = null) }
                                FullscreenAction()
                            },
                        scrollBehavior = scrollBehavior
                    )
                }
            }
        ) { innerPadding ->
            val topPadding = innerPadding.calculateTopPadding()
            if (buttonStates.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(top = topPadding), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Text(strings.noButtonsAvailable)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize().horizontalCutoutPadding().nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(top = topPadding + 12.dp)
                ) {
                    items(buttonStates) { button ->
                        ButtonMaterial(button, onPressDown, onPressUp)
                    }
                    item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
                }
            }
        }
    }
}

// ── Miuix button: press-down + release + long-press ───────────────────

@Composable
private fun ButtonMiuix(
    button: ButtonState,
    onPressDown: (ButtonState) -> Unit,
    onPressUp: (ButtonState) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .squircleBackground(color = MiuixTheme.colorScheme.surfaceContainer, cornerRadius = 16.dp)
            .pointerInput(button.name) {
                detectTapGestures(
                    onPress = {
                        onPressDown(button)
                        tryAwaitRelease()
                        onPressUp(button)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = button.name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                color = MiuixTheme.colorScheme.onSurface,
            )
        }
    }
}

// ── Material button: press-down + release + long-press ────────────────

@Composable
private fun ButtonMaterial(
    button: ButtonState,
    onPressDown: (ButtonState) -> Unit,
    onPressUp: (ButtonState) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(button.name) {
                detectTapGestures(
                    onPress = {
                        onPressDown(button)
                        tryAwaitRelease()
                        onPressUp(button)
                    },
                )
            },
    ) {
        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.material3.MaterialTheme.shapes.medium,
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
}
