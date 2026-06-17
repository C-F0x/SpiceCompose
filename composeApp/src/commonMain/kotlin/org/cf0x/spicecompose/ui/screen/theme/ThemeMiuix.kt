package org.cf0x.spicecompose.ui.screen.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import org.cf0x.spicecompose.ui.i18n.AppStrings
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.navigation.NavLayoutMode
import org.cf0x.spicecompose.ui.theme.ColorMode
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.*
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun ThemeScreenMiuix(uiState: ThemeUiState, actions: ThemeScreenActions) {
    val scrollBehavior = MiuixScrollBehavior()
    val strings        = LocalAppStrings.current

    var showAccentPicker by rememberSaveable { mutableStateOf(false) }
    // Local scale value — only committed on slider release
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
            TopAppBar(
                title          = strings.themeSettings,
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(MiuixIcons.Back, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        popupHost = {},
    ) { innerPadding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp),
            contentPadding = innerPadding,
            overscrollEffect = null,
        ) {
            item {
                Spacer(Modifier.height(12.dp))

                // ── Phone preview ────────────────────────────────────────────
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ThemePreviewCard(navLayoutMode = uiState.navLayoutMode)
                }
                Spacer(Modifier.height(16.dp))

                // ── 3 mode chips (Auto | Light | Dark) ───────────
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorModeChip(Icons.Rounded.Star,      uiState.colorMode == ColorMode.SYSTEM, Modifier.weight(1f)) { actions.onSetColorMode(ColorMode.SYSTEM) }
                    ColorModeChip(Icons.Rounded.LightMode, uiState.colorMode == ColorMode.LIGHT,  Modifier.weight(1f)) { actions.onSetColorMode(ColorMode.LIGHT) }
                    ColorModeChip(Icons.Rounded.DarkMode,  uiState.colorMode == ColorMode.DARK,   Modifier.weight(1f)) { actions.onSetColorMode(ColorMode.DARK) }
                }
                Spacer(Modifier.height(12.dp))

                // ── Color section ────────────────────────────────────────────
                Card(modifier = Modifier.fillMaxWidth()) {
                    // Enable Monet
                    SwitchPreference(
                        title   = strings.monetEnable,
                        summary = strings.monetEnableSummary,
                        checked = uiState.useMonet,
                        onCheckedChange = actions.onSetUseMonet,
                        startAction = { PrefIcon(Icons.Rounded.Wallpaper) },
                    )

                    // AMOLED Dark
                    if (uiState.colorMode != ColorMode.LIGHT) {
                        SwitchPreference(
                            title   = strings.amoledDark,
                            summary = strings.amoledDarkSummary,
                            checked = uiState.amoledDark,
                            onCheckedChange = actions.onSetAmoledDark,
                            startAction = { PrefIcon(Icons.Rounded.Brightness1) },
                        )
                    }

                    // ── Monet ON: palette style + accent color ──────
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
                Spacer(Modifier.height(12.dp))

                // ── Layout ───────────────────────────────────────────────────
                Card(modifier = Modifier.fillMaxWidth()) {
                    // Nav mode: disabled when floating bar is on
                    OverlayDropdownPreference(
                        title   = strings.navBarStyle,
                        summary = listOf(strings.navAuto, strings.navBottom, strings.navRail)[uiState.navLayoutMode.ordinal],
                        items   = listOf(strings.navAuto, strings.navBottom, strings.navRail),
                        selectedIndex = uiState.navLayoutMode.ordinal,
                        onSelectedIndexChange = { actions.onSetNavLayoutMode(NavLayoutMode.entries[it]) },
                        startAction = { PrefIcon(Icons.AutoMirrored.Rounded.MenuOpen) },
                        enabled = !uiState.floatingBottomBar
                    )

                    // Floating bar: Forced to Bottom Bar when enabled
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

                    // Liquid Glass sub-option
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
                Spacer(Modifier.height(12.dp))

                // ── Effects (Miuix-only: blur) ───────────────────────────────
                Card(modifier = Modifier.fillMaxWidth()) {
                    SwitchPreference(
                        title   = strings.enableBlur,
                        summary = strings.enableBlurSummary,
                        checked = uiState.enableBlur,
                        onCheckedChange = actions.onSetEnableBlur,
                        startAction = { PrefIcon(Icons.Rounded.BlurOn) },
                    )
                }
                Spacer(Modifier.height(12.dp))

                // ── Scale (inline slider, apply on release) ──────────────────
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PrefIcon(Icons.Rounded.AspectRatio)
                            Column(Modifier.weight(1f)) {
                                Text(strings.pageScale,   style = textStyles.main, color = colorScheme.onBackground)
                                Text(strings.pageScaleSummary, style = textStyles.main, color = colorScheme.onSurfaceVariantSummary)
                            }
                            Text("${"%.0f".format(localScale * 100)}%",
                                style = textStyles.main, color = colorScheme.onSurfaceVariantSummary)
                        }
                        Slider(
                            value              = localScale,
                            onValueChange      = { localScale = it },           // update local preview
                            onValueChangeFinished = { actions.onSetPageScale(localScale) }, // commit on release
                            valueRange         = 0.6f..1.4f,
                            modifier           = Modifier.fillMaxWidth().padding(top = 4.dp),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
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
) { Icon(icon, null, tint = if (selected) colorScheme.onPrimary else colorScheme.onSecondaryContainer,
    modifier = Modifier.size(22.dp)) }

@Composable
private fun PrefIcon(icon: ImageVector) =
    Icon(icon, null, Modifier.padding(end = 6.dp), colorScheme.onBackground)

private fun paletteStyleLabels(s: AppStrings) = listOf(
    s.paletteTonalSpot, s.paletteNeutral, s.paletteVibrant, s.paletteExpressive,
    s.paletteRainbow, s.paletteFruitSalad, s.paletteMonochrome, s.paletteFidelity, s.paletteContent,
)
