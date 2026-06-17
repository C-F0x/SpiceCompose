package org.cf0x.spicecompose.ui.screen.utils.subscreen

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@Composable
fun SubScreenMiuix(
    onBack: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val scrollBehavior = MiuixScrollBehavior()
    val fullscreen = LocalFullscreenMode.current

    SpiceBackHandler(enabled = fullscreen.value) {
        fullscreen.value = false
    }

    Scaffold(
        topBar = {
            if (!fullscreen.value) {
                SmallTopAppBar(
                    title = strings.subScreen,
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(MiuixIcons.Back, contentDescription = null)
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
