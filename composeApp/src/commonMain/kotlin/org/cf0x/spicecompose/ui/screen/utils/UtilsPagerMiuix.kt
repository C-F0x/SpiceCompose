package org.cf0x.spicecompose.ui.screen.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun UtilsPagerMiuix(
    actions: UtilsScreenActions,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val strings = LocalAppStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = strings.utils,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp),
            contentPadding = innerPadding,
        ) {
            item {
                Card(
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                ) {
                    ArrowPreference(
                        title = strings.subScreen,
                        summary = strings.subScreenSummary,
                        startAction = {
                            Icon(Icons.Rounded.Tv, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = actions.onOpenSubScreen,
                    )
                }

                Card(
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                ) {
                    ArrowPreference(
                        title = strings.patches,
                        summary = strings.patchesSummary,
                        startAction = {
                            Icon(Icons.Rounded.Build, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = actions.onOpenPatches,
                    )
                }
            }
        }
    }
}
