package org.cf0x.spicecompose.ui.screen.tools

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LinearScale
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.ScreenshotMonitor
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.theme.LocalEnableBlur
import org.cf0x.spicecompose.ui.util.BlurredBar
import org.cf0x.spicecompose.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import org.cf0x.spicecompose.ui.theme.LocalDevMode

@Composable
fun ToolsPagerMiuix(
    actions: ToolsScreenActions,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur && LocalUiMode.current == UiMode.Miuix)
    val blurActive = backdrop != null
    val barColor = if (blurActive) Color.Transparent else colorScheme.surface
    val strings = LocalAppStrings.current
    val devMode = LocalDevMode.current
    Scaffold(
        topBar = {
            BlurredBar(backdrop, blurActive) {
                TopAppBar(
                    title = strings.tools,
                    actions = {},
                    color = barColor,
                    scrollBehavior = scrollBehavior,
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding()),
        ) {
            // ── Top spacing ──────────────────────────────────────────────────
            item { Spacer(Modifier.height(12.dp)) }

            item {
                Card(
                    modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth(),
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
            }

            item {
                Card(
                    modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth(),
                ) {
                    ArrowPreference(
                        title = strings.cabinetUtility,
                        summary = strings.keypadSummary,
                        startAction = {
                            Icon(Icons.Rounded.Apps, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = { maybeVibrate(15); actions.onOpenCabinetUtility() },
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth(),
                ) {
                    ArrowPreference(
                        title = strings.subScreen,
                        summary = strings.subScreenSummary,
                        startAction = {
                            Icon(Icons.Rounded.ScreenshotMonitor, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = { maybeVibrate(15); actions.onOpenSubScreen() },
                    )
                }
            }

            // ── Patches (dev mode) ───────────────────────────────────
            if (devMode) {
                item {
                    Card(modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth()) {
                        ArrowPreference(
                            title = strings.patches,
                            summary = strings.patchesSummary,
                            startAction = { Icon(Icons.Rounded.Build, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground) },
                            onClick = { maybeVibrate(15); actions.onOpenPatches() },
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth(),
                ) {
                    ArrowPreference(
                        title = "${strings.gameController}${strings.betaSuffix}",
                        summary = strings.gameControllerSummary,
                        startAction = {
                            Icon(Icons.Rounded.Gamepad, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground)
                        },
                        onClick = { maybeVibrate(15); actions.onOpenController() },
                    )
                }
            }

            // ── LCD + Resize (dev mode) ──────────────────────────────
            if (devMode) {
                item {
                    Card(modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth()) {
                        ArrowPreference(
                            title = strings.lcdInfo, summary = strings.lcdInfoSummary,
                            startAction = { Icon(Icons.Rounded.Tv, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground) },
                            onClick = { maybeVibrate(15); actions.onOpenLcd() },
                        )
                        ArrowPreference(
                            title = strings.screenResize, summary = strings.screenResizeSummary,
                            startAction = { Icon(Icons.Rounded.AspectRatio, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground) },
                            onClick = { maybeVibrate(15); actions.onOpenResize() },
                        )
                    }
                }
            }

            // ── DIY Controller (dev mode) ────────────────────────────
            if (devMode) {
                item {
                    Card(modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth()) {
                        ArrowPreference(
                            title = strings.diyController, summary = strings.diyControllerSummary,
                            startAction = { Icon(Icons.Rounded.DashboardCustomize, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground) },
                            onClick = { maybeVibrate(15); actions.onOpenDiy() },
                        )
                    }
                }
            }

            // ── Bottom spacing ───────────────────────────────────────────────
            item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
        }
    }
}
