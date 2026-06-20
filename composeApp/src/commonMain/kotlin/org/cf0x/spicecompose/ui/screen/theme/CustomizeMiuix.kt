package org.cf0x.spicecompose.ui.screen.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Brightness1
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.WebAsset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.platform.maybeVibrate
import org.cf0x.spicecompose.platform.vibrationAvailable
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.i18n.AppStrings
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.navigation.NavLayoutMode
import org.cf0x.spicecompose.ui.theme.ColorMode
import org.cf0x.spicecompose.ui.theme.ThemePreferences
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

private val BlockSpacing = 8.dp

@Composable
fun CustomizeScreenMiuix(uiState: CustomizeUiState, actions: CustomizeScreenActions) {
    val scrollBehavior = MiuixScrollBehavior()
    val strings        = LocalAppStrings.current
    val fullscreen     = LocalFullscreenMode.current
    val p              = ThemePreferences
    val scope          = rememberCoroutineScope()

    SpiceBackHandler(enabled = fullscreen.value) {
        fullscreen.value = false
    }

    var showAccentPicker by rememberSaveable { mutableStateOf(false) }
    var localScale by remember(uiState.pageScale) { mutableFloatStateOf(uiState.pageScale) }

    if (showAccentPicker) {
        SpiceAccentColorDialog(
            current   = uiState.keyColor,
            onConfirm = { actions.onSetKeyColor(it); showAccentPicker = false },
            onDismiss = { showAccentPicker = false },
        )
    }

    Scaffold(
        topBar = {
            if (!fullscreen.value && !p.toolbarHidden) {
                TopAppBar(
                    title          = strings.themeSettings,
                    navigationIcon = {
                        IconButton(onClick = actions.onBack) {
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
        popupHost = {},
    ) { innerPadding ->
        val padding = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp),
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(BlockSpacing),
            overscrollEffect = null,
        ) {
            // ── Top spacing ──────────────────────────────────────────────────
            item { Spacer(Modifier.height(BlockSpacing)) }

            // ── Phone preview ────────────────────────────────────────────────
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ThemePreviewCard(navLayoutMode = uiState.navLayoutMode)
                }
            }

            // ── 3 mode chips (Auto | Light | Dark) ───────────────────────────
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorModeChip(Icons.Rounded.Star,      uiState.colorMode == ColorMode.SYSTEM, Modifier.weight(1f)) { actions.onSetColorMode(ColorMode.SYSTEM) }
                    ColorModeChip(Icons.Rounded.LightMode, uiState.colorMode == ColorMode.LIGHT,  Modifier.weight(1f)) { actions.onSetColorMode(ColorMode.LIGHT) }
                    ColorModeChip(Icons.Rounded.DarkMode,  uiState.colorMode == ColorMode.DARK,   Modifier.weight(1f)) { actions.onSetColorMode(ColorMode.DARK) }
                }
            }

            // ── Color section ────────────────────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    SwitchPreference(
                        title   = strings.monetEnable,
                        summary = strings.monetEnableSummary,
                        checked = uiState.useMonet,
                        onCheckedChange = actions.onSetUseMonet,
                        startAction = { PrefIcon(Icons.Rounded.Wallpaper) },
                    )
                    if (uiState.colorMode != ColorMode.LIGHT) {
                        SwitchPreference(
                            title   = strings.amoledDark,
                            summary = strings.amoledDarkSummary,
                            checked = uiState.amoledDark,
                            onCheckedChange = actions.onSetAmoledDark,
                            startAction = { PrefIcon(Icons.Rounded.Brightness1) },
                        )
                    }
                    if (uiState.useMonet) {
                        val paletteLabels = paletteStyleLabels(strings)
                        OverlayDropdownPreference(
                            title   = strings.paletteStyle,
                            summary = paletteLabels[PaletteStyle.entries.indexOf(uiState.paletteStyle).coerceAtLeast(0)],
                            items   = paletteLabels,
                            selectedIndex = PaletteStyle.entries.indexOf(uiState.paletteStyle).coerceAtLeast(0),
                            onSelectedIndexChange = { actions.onSetPaletteStyle(PaletteStyle.entries[it]) },
                            startAction = { PrefIcon(Icons.Rounded.ColorLens) },
                        )
                        ArrowPreference(
                            title   = strings.keyColor,
                            summary = strings.keyColorSummary,
                            startAction = {
                                Box(
                                    Modifier.padding(end = 6.dp).size(24.dp)
                                        .clip(CircleShape).background(uiState.keyColor)
                                )
                            },
                            onClick = { showAccentPicker = true },
                        )
                    }
                }
            }

            // ── Layout ───────────────────────────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    OverlayDropdownPreference(
                        title   = strings.navBarStyle,
                        summary = listOf(strings.navAuto, strings.navBottom, strings.navRail)[uiState.navLayoutMode.ordinal],
                        items   = listOf(strings.navAuto, strings.navBottom, strings.navRail),
                        selectedIndex = uiState.navLayoutMode.ordinal,
                        onSelectedIndexChange = { actions.onSetNavLayoutMode(NavLayoutMode.entries[it]) },
                        startAction = { PrefIcon(Icons.AutoMirrored.Rounded.MenuOpen) },
                        enabled = !uiState.floatingBottomBar
                    )
                    SwitchPreference(
                        title   = strings.floatingBottomBar,
                        summary = strings.floatingBottomBarSummary,
                        checked = uiState.floatingBottomBar,
                        onCheckedChange = { enabled ->
                            actions.onSetFloatingBottomBar(enabled)
                            if (enabled) actions.onSetNavLayoutMode(NavLayoutMode.BottomBar)
                        },
                        startAction = { PrefIcon(Icons.Rounded.WebAsset) },
                    )
                    if (uiState.floatingBottomBar) {
                        SwitchPreference(
                            title   = strings.liquidGlass,
                            summary = strings.liquidGlassSummary,
                            checked = uiState.floatingBottomBarBlur,
                            onCheckedChange = actions.onSetFloatingBottomBarBlur,
                            startAction = { PrefIcon(Icons.Rounded.BlurOn) },
                        )
                    }
                }
            }

            // ── Effects (Miuix-only: blur) ───────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    SwitchPreference(
                        title   = strings.enableBlur,
                        summary = strings.enableBlurSummary,
                        checked = uiState.enableBlur,
                        onCheckedChange = actions.onSetEnableBlur,
                        startAction = { PrefIcon(Icons.Rounded.BlurOn) },
                    )
                }
            }

            // ── Scale (inline slider, apply on release) ──────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PrefIcon(Icons.Rounded.AspectRatio)
                            Column(Modifier.weight(1f)) {
                                Text(strings.pageScale,   style = textStyles.main, color = colorScheme.onBackground)
                                Text(strings.pageScaleSummary, style = textStyles.main, color = colorScheme.onSurfaceVariantSummary)
                            }
                            Text(
                                "${(localScale * 100).toInt()}%",
                                style = textStyles.main, color = colorScheme.onSurfaceVariantSummary
                            )
                        }
                        Slider(
                            value              = localScale,
                            onValueChange      = { localScale = it },
                            onValueChangeFinished = { actions.onSetPageScale(localScale) },
                            valueRange         = 0.6f..1.4f,
                            modifier           = Modifier.fillMaxWidth().padding(top = 4.dp),
                        )
                    }
                }
            }

            // ── Vibration ────────────────────────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        ArrowPreference(
                            title = "Vibration",
                            summary = if (p.vibrationEnabled) "On" else "Off",
                            startAction = {
                                Icon(
                                    Icons.Rounded.Vibration, null,
                                    Modifier.padding(end = 6.dp), colorScheme.onBackground
                                )
                            },
                            onClick = { p.updateVibrationEnabled(!p.vibrationEnabled) }
                        )
                        if (vibrationAvailable && p.vibrationEnabled) {
                            Text("${p.vibDuration}ms", Modifier.padding(top = 8.dp))
                            Slider(
                                value = p.vibDuration.toFloat(),
                                onValueChange = { p.updateVibDuration(it.toInt()) },
                                valueRange = 0f..200f,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                text = "Test ${p.vibDuration}ms",
                                onClick = { scope.launch { maybeVibrate(p.vibDuration.toLong()) } },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // ── Bottom spacing ───────────────────────────────────────────────
            item { Spacer(Modifier.height(BlockSpacing)) }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun ColorModeChip(
    icon: ImageVector, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit,
) = Box(
    modifier = modifier.height(48.dp)
        .clip(RoundedCornerShape(24.dp))
        .background(if (selected) colorScheme.primary else colorScheme.secondaryContainer)
        .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
) {
    Icon(
        icon, null,
        tint = if (selected) colorScheme.onPrimary else colorScheme.onSecondaryContainer,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
private fun PrefIcon(icon: ImageVector) =
    Icon(icon, null, Modifier.padding(end = 6.dp), colorScheme.onBackground)

@Composable
private fun VibrationCard() {
    val scope = rememberCoroutineScope()
    val p = ThemePreferences
    val enabled = vibrationAvailable

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            ArrowPreference(
                title = "Vibration Test",
                summary = if (enabled) "${p.vibDuration}ms" else "Not supported",
                startAction = {
                    Icon(
                        Icons.Rounded.Vibration, null, Modifier.padding(end = 6.dp),
                        tint = if (enabled) colorScheme.onBackground else colorScheme.onBackground.copy(alpha = 0.3f)
                    )
                }
            )
            if (enabled) {
                Text("${p.vibDuration}ms", Modifier.padding(bottom = 4.dp))
                Slider(
                    value = p.vibDuration.toFloat(),
                    onValueChange = { p.updateVibDuration(it.toInt()) },
                    valueRange = 0f..200f,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    text = "Test ${p.vibDuration}ms",
                    onClick = { scope.launch { maybeVibrate(p.vibDuration.toLong()) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ToggleCard(
    title: String, checked: Boolean, onToggle: (Boolean) -> Unit,
    icon: ImageVector = Icons.Rounded.Visibility
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ArrowPreference(
            title = title,
            summary = if (checked) "On" else "Off",
            startAction = { Icon(icon, null, Modifier.padding(end = 6.dp), colorScheme.onBackground) },
            onClick = { onToggle(!checked) }
        )
    }
}

private fun paletteStyleLabels(s: AppStrings) = listOf(
    s.paletteTonalSpot, s.paletteNeutral, s.paletteVibrant, s.paletteExpressive,
    s.paletteRainbow, s.paletteFruitSalad, s.paletteMonochrome, s.paletteFidelity, s.paletteContent,
)
