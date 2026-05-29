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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
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
import org.cf0x.spicecompose.ui.theme.keyColorPresets
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
        AccentColorDialog(
            current   = uiState.keyColor,
            onConfirm = { actions.onSetKeyColor(it); showAccentPicker = false },
            onDismiss = { showAccentPicker = false },
            strings   = strings,
        )
    }

    // Derived nav/floating mutual-exclusion logic
    val effectiveNav = uiState.navLayoutMode   // Auto, BottomBar, SideRail
    val isBottomMode = effectiveNav == NavLayoutMode.Auto || effectiveNav == NavLayoutMode.BottomBar
    val monetEnabled = uiState.colorMode.isMonet

    fun resolveColorMode(baseMonet: Boolean): ColorMode = when {
        baseMonet && uiState.colorMode.isLight -> ColorMode.MONET_LIGHT
        baseMonet && uiState.colorMode.isDark  -> ColorMode.MONET_DARK
        baseMonet                              -> ColorMode.MONET_SYSTEM
        uiState.colorMode == ColorMode.DARK_AMOLED -> ColorMode.DARK_AMOLED
        uiState.colorMode.isLight              -> ColorMode.LIGHT
        uiState.colorMode.isDark               -> ColorMode.DARK
        else                                   -> ColorMode.SYSTEM
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

                // ── 4 mode chips (Dynamic | Light | Dark | AMOLED) ───────────
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorModeChip(Icons.Rounded.AutoAwesome,  monetEnabled,                              Modifier.weight(1f)) { actions.onSetColorMode(ColorMode.MONET_SYSTEM) }
                    ColorModeChip(Icons.Rounded.LightMode,    uiState.colorMode == ColorMode.LIGHT,      Modifier.weight(1f)) { actions.onSetColorMode(ColorMode.LIGHT) }
                    ColorModeChip(Icons.Rounded.DarkMode,     uiState.colorMode == ColorMode.DARK,       Modifier.weight(1f)) { actions.onSetColorMode(ColorMode.DARK) }
                    ColorModeChip(Icons.Rounded.Brightness1,  uiState.colorMode == ColorMode.DARK_AMOLED,Modifier.weight(1f)) { actions.onSetColorMode(ColorMode.DARK_AMOLED) }
                }
                Spacer(Modifier.height(12.dp))

                // ── Color section ────────────────────────────────────────────
                Card(modifier = Modifier.fillMaxWidth()) {
                    // Enable Monet
                    SwitchPreference(
                        title   = strings.monetEnable,
                        summary = strings.monetEnableSummary,
                        checked = monetEnabled,
                        onCheckedChange = { actions.onSetColorMode(resolveColorMode(it)) },
                        startAction = { PrefIcon(Icons.Rounded.Wallpaper) },
                    )

                    // Accent color (only when Monet is OFF)
                    if (!monetEnabled) {
                        ArrowPreference(
                            title   = strings.keyColor,
                            summary = strings.keyColorSummary,
                            startAction = {
                                Box(
                                    Modifier.size(20.dp).padding(end = 2.dp)
                                        .clip(CircleShape).background(uiState.keyColor)
                                )
                            },
                            onClick = { showAccentPicker = true },
                        )
                    }

                    // Palette style (hide when Monet)
                    if (!monetEnabled) {
                        val paletteLabels = paletteStyleLabels(strings)
                        OverlayDropdownPreference(
                            title   = strings.paletteStyle,
                            summary = paletteLabels[PaletteStyle.entries.indexOf(uiState.paletteStyle).coerceAtLeast(0)],
                            items   = paletteLabels,
                            selectedIndex = PaletteStyle.entries.indexOf(uiState.paletteStyle).coerceAtLeast(0),
                            onSelectedIndexChange = { actions.onSetPaletteStyle(PaletteStyle.entries[it]) },
                            startAction = { PrefIcon(Icons.Rounded.ColorLens) },
                        )
                        OverlayDropdownPreference(
                            title   = strings.colorSpec,
                            summary = if (uiState.colorSpecVersion == ColorSpec.SpecVersion.SPEC_2025) strings.spec2025 else strings.spec2021,
                            items   = listOf(strings.spec2021, strings.spec2025),
                            selectedIndex = if (uiState.colorSpecVersion == ColorSpec.SpecVersion.SPEC_2025) 1 else 0,
                            onSelectedIndexChange = {
                                actions.onSetColorSpecVersion(if (it == 1) ColorSpec.SpecVersion.SPEC_2025 else ColorSpec.SpecVersion.SPEC_2021)
                            },
                            startAction = { PrefIcon(Icons.Rounded.Science) },
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                // ── Layout ───────────────────────────────────────────────────
                Card(modifier = Modifier.fillMaxWidth()) {
                    // Nav mode: hide when floating bar is on (mutually exclusive)
                    if (!uiState.floatingBottomBar) {
                        OverlayDropdownPreference(
                            title   = strings.navBarStyle,
                            summary = listOf(strings.navAuto, strings.navBottom, strings.navRail)[uiState.navLayoutMode.ordinal],
                            items   = listOf(strings.navAuto, strings.navBottom, strings.navRail),
                            selectedIndex = uiState.navLayoutMode.ordinal,
                            onSelectedIndexChange = { actions.onSetNavLayoutMode(NavLayoutMode.entries[it]) },
                            startAction = { PrefIcon(Icons.AutoMirrored.Rounded.MenuOpen) },
                        )
                    }

                    /*
                    // Floating bar: only show when actual nav mode is bottom (not rail)
                    if (isBottomMode) {
                        SwitchPreference(
                            title   = strings.floatingBottomBar,
                            summary = strings.floatingBottomBarSummary,
                            checked = uiState.floatingBottomBar,
                            onCheckedChange = actions.onSetFloatingBottomBar,
                            startAction = { PrefIcon(Icons.Rounded.WebAsset) },
                        )
                        // Blur sub-option (hidden when no floating bar)
                        if (uiState.floatingBottomBar) {
                            SwitchPreference(
                                title   = strings.floatingBottomBarBlur,
                                summary = strings.floatingBottomBarBlurSummary,
                                checked = uiState.floatingBottomBarBlur,
                                onCheckedChange = actions.onSetFloatingBottomBarBlur,
                                startAction = { PrefIcon(Icons.Rounded.BlurOn) },
                            )
                        }
                    }
                    */
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

@Composable
private fun AccentColorDialog(
    current: Color, onConfirm: (Color) -> Unit, onDismiss: () -> Unit, strings: AppStrings,
) {
    var hexInput by remember { mutableStateOf(current.toHex()) }
    var isError  by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { androidx.compose.material3.Text(strings.keyColor) },
        text = {
            androidx.compose.foundation.lazy.LazyColumn {
                // "Default" option
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onConfirm(org.cf0x.spicecompose.ui.theme.defaultKeyColor) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(Modifier.size(32.dp).clip(CircleShape).background(org.cf0x.spicecompose.ui.theme.defaultKeyColor)
                            .border(2.dp, if (current == org.cf0x.spicecompose.ui.theme.defaultKeyColor) colorScheme.primary else Color.Transparent, CircleShape))
                        androidx.compose.material3.Text(strings.specDefault)
                    }
                }
                // Presets in rows of 5
                item {
                    val rows = keyColorPresets.chunked(5)
                    rows.forEach { row ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { opt ->
                                Box(Modifier.size(36.dp).clip(CircleShape).background(opt.color)
                                    .border(2.dp, if (current == opt.color) colorScheme.primary else Color.Transparent, CircleShape)
                                    .clickable { onConfirm(opt.color) },
                                    contentAlignment = Alignment.Center) {
                                    if (current == opt.color) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
                // Custom HEX input
                item {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = hexInput, onValueChange = { hexInput = it.take(6).uppercase(); isError = false },
                        label = { androidx.compose.material3.Text("#RRGGBB") }, isError = isError, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Box(Modifier.size(24.dp).clip(CircleShape).background(
                                runCatching { Color(("FF$hexInput").toLong(16)) }.getOrNull() ?: Color.Gray))
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val c = runCatching { Color(("FF$hexInput").toLong(16)) }.getOrNull()
                if (c != null) onConfirm(c) else isError = true
            }) { androidx.compose.material3.Text(strings.ok) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { androidx.compose.material3.Text(strings.cancel) }
        },
    )
}

private fun Color.toHex(): String =
    (value.toLong() and 0xFFFFFF).toString(16).uppercase().padStart(6, '0')

private fun paletteStyleLabels(s: AppStrings) = listOf(
    s.paletteTonalSpot, s.paletteNeutral, s.paletteVibrant, s.paletteExpressive,
    s.paletteRainbow, s.paletteFruitSalad, s.paletteMonochrome, s.paletteFidelity, s.paletteContent,
)
