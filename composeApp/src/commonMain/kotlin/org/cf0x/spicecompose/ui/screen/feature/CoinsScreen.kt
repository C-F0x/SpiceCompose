package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.coinInsert
import org.cf0x.spicecompose.platform.LocalFullscreenMode
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
fun CoinsScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val connectionManager = LocalConnectionManager.current
    val connection = connectionManager.getClient()
    val scope = rememberCoroutineScope()
    val fullscreen = LocalFullscreenMode.current
    val p = ThemePreferences
    
    SpiceBackHandler(enabled = fullscreen.value) {
        fullscreen.value = false
    }

    val onInsert: (Int) -> Unit = { amount ->
        scope.launch {
            connection?.coinInsert(amount)
        }
    }

    when (LocalUiMode.current) {
        UiMode.Miuix -> {
            top.yukonga.miuix.kmp.basic.Scaffold(
                topBar = {
                    if (!fullscreen.value && !p.toolbarHidden) {
                        SmallTopAppBar(
                            title = strings.coins,
                            navigationIcon = {
                                top.yukonga.miuix.kmp.basic.IconButton(onClick = onBack) {
                                    top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null)
                                }
                            },
                            actions = {
                                FullscreenAction()
                            }
                        )
                    }
                }
            ) { innerPadding ->
                val padding = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
                LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                    item {
                        CoinActionMiuix("Insert 1 Coin", { onInsert(1) })
                        CoinActionMiuix("Insert 5 Coins", { onInsert(5) })
                        CoinActionMiuix("Insert 10 Coins", { onInsert(10) })
                    }
                }
            }
        }
        UiMode.Material -> {
            androidx.compose.material3.Scaffold(
                topBar = {
                    if (!fullscreen.value && !p.toolbarHidden) {
                        @OptIn(ExperimentalMaterial3Api::class)
                        androidx.compose.material3.TopAppBar(
                            title = { androidx.compose.material3.Text(strings.coins) },
                            navigationIcon = {
                                androidx.compose.material3.IconButton(onClick = onBack) {
                                    androidx.compose.material3.Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                                }
                            },
                            actions = {
                                FullscreenAction()
                            }
                        )
                    }
                }
            ) { innerPadding ->
                val padding = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
                LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                    item {
                        CoinActionMaterial("Insert 1 Coin", { onInsert(1) })
                        CoinActionMaterial("Insert 5 Coins", { onInsert(5) })
                        CoinActionMaterial("Insert 10 Coins", { onInsert(10) })
                    }
                }
            }
        }
    }
}

@Composable
fun CoinActionMiuix(text: String, onClick: () -> Unit) {
    top.yukonga.miuix.kmp.basic.Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        onClick = onClick
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.Add, null)
            Spacer(Modifier.width(12.dp))
            top.yukonga.miuix.kmp.basic.Text(text)
        }
    }
}

@Composable
fun CoinActionMaterial(text: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { androidx.compose.material3.Text(text) },
        leadingContent = { androidx.compose.material3.Icon(Icons.Rounded.Add, null) }
    )
}
