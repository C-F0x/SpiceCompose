package org.cf0x.spicecompose.ui.screen.utils.subscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScreenMaterial(
    onBack: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val fullscreen = LocalFullscreenMode.current

    SpiceBackHandler(enabled = fullscreen.value) {
        fullscreen.value = false
    }

    Scaffold(
        topBar = {
            if (!fullscreen.value) {
                TopAppBar(
                    title = { Text(strings.subScreen) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
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
            SubScreenContent()
        }
    }
}
