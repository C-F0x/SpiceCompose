package org.cf0x.spicecompose.ui.screen.feature

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.spiceapi.wrappers.cardInsert
import org.cf0x.spicecompose.network.spiceapi.wrappers.keypadsWrite
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@Composable
fun KeypadScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val connectionManager = LocalConnectionManager.current
    val connection = connectionManager.getConnection()
    val scope = rememberCoroutineScope()
    
    var currentMode by remember { mutableIntStateOf(0) } // 0: P1, 1: P2
    val modeColor = if (currentMode == 0) Color(0xFF008080) else Color(0xFF800080)
    val modeLabel = if (currentMode == 0) "P1" else "P2"

    val onKeyClick: (String) -> Unit = { key ->
        scope.launch {
            connection?.keypadsWrite(currentMode, key)
        }
    }

    val onInsertCard: () -> Unit = {
        scope.launch {
            // Dummy card ID for now
            connection?.cardInsert(currentMode, "1234567890123456")
        }
    }

    val keys = listOf(
        "7", "8", "9",
        "4", "5", "6",
        "1", "2", "3",
        "0", "00", "."
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> {
            top.yukonga.miuix.kmp.basic.Scaffold(
                topBar = {
                    SmallTopAppBar(
                        title = strings.keypad,
                        navigationIcon = {
                            top.yukonga.miuix.kmp.basic.IconButton(onClick = onBack) {
                                top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null)
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Column(Modifier.fillMaxSize().padding(innerPadding)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(keys) { key ->
                            KeyButtonMiuix(key) { onKeyClick(if (key == ".") "D" else if (key == "00") "A" else key) }
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        top.yukonga.miuix.kmp.basic.TextButton(
                            text = modeLabel,
                            onClick = { currentMode = (currentMode + 1) % 2 },
                            modifier = Modifier.weight(1f),
                            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary()
                        )
                        top.yukonga.miuix.kmp.basic.TextButton(
                            text = "Insert Card",
                            onClick = onInsertCard,
                            modifier = Modifier.weight(1f),
                            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary()
                        )
                    }
                }
            }
        }
        UiMode.Material -> {
            androidx.compose.material3.Scaffold(
                topBar = {
                    @OptIn(ExperimentalMaterial3Api::class)
                    androidx.compose.material3.TopAppBar(
                        title = { androidx.compose.material3.Text(strings.keypad) },
                        navigationIcon = {
                            androidx.compose.material3.IconButton(onClick = onBack) {
                                androidx.compose.material3.Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Column(Modifier.fillMaxSize().padding(innerPadding)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(keys) { key ->
                            KeyButtonMaterial(key) { onKeyClick(if (key == ".") "D" else if (key == "00") "A" else key) }
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        androidx.compose.material3.Button(
                            onClick = { currentMode = (currentMode + 1) % 2 },
                            modifier = Modifier.weight(1f),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = modeColor)
                        ) {
                            androidx.compose.material3.Text(modeLabel)
                        }
                        androidx.compose.material3.Button(
                            onClick = onInsertCard,
                            modifier = Modifier.weight(1f)
                        ) {
                            androidx.compose.material3.Text("Insert Card")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeyButtonMiuix(label: String, onClick: () -> Unit) {
    top.yukonga.miuix.kmp.basic.Card(
        modifier = Modifier.aspectRatio(1.5f),
        onClick = onClick
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            top.yukonga.miuix.kmp.basic.Text(label, fontSize = 28.sp)
        }
    }
}

@Composable
fun KeyButtonMaterial(label: String, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.aspectRatio(1.5f),
        onClick = onClick
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            androidx.compose.material3.Text(label, fontSize = 28.sp)
        }
    }
}
