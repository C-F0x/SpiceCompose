package org.cf0x.spicecompose.ui.screen.tools

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
fun ToolsPagerMiuix(
    actions: ToolsScreenActions,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val strings = LocalAppStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = strings.tools,
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
                        title = strings.buttons,
                        summary = strings.buttonsSummary,
                        startAction = {
                            Icon(Icons.Rounded.RadioButtonChecked, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = actions.onOpenButtons,
                    )
                    ArrowPreference(
                        title = strings.analogs,
                        summary = strings.analogsSummary,
                        startAction = {
                            Icon(Icons.Rounded.LinearScale, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = actions.onOpenAnalogs,
                    )
                    ArrowPreference(
                        title = strings.lights,
                        summary = strings.lightsSummary,
                        startAction = {
                            Icon(Icons.Rounded.Lightbulb, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = actions.onOpenLights,
                    )
                }

                Card(
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                ) {
                    ArrowPreference(
                        title = strings.coins,
                        summary = strings.coinsSummary,
                        startAction = {
                            Icon(Icons.Rounded.MonetizationOn, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = actions.onOpenCoins,
                    )
                    ArrowPreference(
                        title = strings.keypad,
                        summary = strings.keypadSummary,
                        startAction = {
                            Icon(Icons.Rounded.Apps, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = actions.onOpenKeypad,
                    )
                }
            }
        }
    }
}
