package org.cf0x.spicecompose.ui.screen.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LinearScale
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.ScreenshotMonitor
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.platform.maybeVibrate
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
                    actions = {},
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
                        onClick = { maybeVibrate(15); actions.onOpenButtons() },
                    )
                    ArrowPreference(
                        title = strings.analogs,
                        summary = strings.analogsSummary,
                        startAction = {
                            Icon(Icons.Rounded.LinearScale, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = { maybeVibrate(15); actions.onOpenAnalogs() },
                    )
                    ArrowPreference(
                        title = strings.lights,
                        summary = strings.lightsSummary,
                        startAction = {
                            Icon(Icons.Rounded.Lightbulb, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = { maybeVibrate(15); actions.onOpenLights() },
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
                        onClick = { maybeVibrate(15); actions.onOpenCoins() },
                    )
                    ArrowPreference(
                        title = strings.keypad,
                        summary = strings.keypadSummary,
                        startAction = {
                            Icon(Icons.Rounded.Apps, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = { maybeVibrate(15); actions.onOpenKeypad() },
                    )
                }

                Card(
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                ) {
                    ArrowPreference(
                        title = strings.subScreen,
                        summary = strings.subScreenSummary,
                        startAction = {
                            Icon(Icons.Rounded.ScreenshotMonitor, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = { maybeVibrate(15); actions.onOpenSubScreen() },
                    )
                    ArrowPreference(
                        title = strings.patches,
                        summary = strings.patchesSummary,
                        startAction = {
                            Icon(Icons.Rounded.Build, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = { maybeVibrate(15); actions.onOpenPatches() },
                    )
                }

                Card(
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                ) {
                    ArrowPreference(
                        title = "Game Controller",
                        summary = "Virtual arcade controller",
                        startAction = {
                            Icon(Icons.Rounded.Gamepad, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = { maybeVibrate(15); actions.onOpenController() },
                    )
                }

                Card(
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                ) {
                    ArrowPreference(
                        title = "LCD Info",
                        summary = "LCD touch panel diagnostics",
                        startAction = {
                            Icon(Icons.Rounded.Tv, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = { maybeVibrate(15); actions.onOpenLcd() },
                    )
                    ArrowPreference(
                        title = "Screen Resize",
                        summary = "Window layout presets",
                        startAction = {
                            Icon(Icons.Rounded.AspectRatio, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = { maybeVibrate(15); actions.onOpenResize() },
                    )
                }
            }
        }
    }
}
