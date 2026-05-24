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

/**
 * Miuix theme settings screen.
 * Layout mirrors KernelSU's Miuix implementation:
 *   - Phone preview mockup
 *   - Seed color circles
 *   - 4 icon mode chips  (Dynamic | Light | Dark | AMOLED)
 *   - PaletteStyle / ColorSpec rows
 *   - Predictive back toggle
 *   - Inline scale slider
 */
@Composable
fun ThemeScreenMiuix(uiState: ThemeUiState, actions: ThemeScreenActions) {
    val scrollBehavior = MiuixScrollBehavior()
    val strings = LocalAppStrings.current
    var showCustomColor by rememberSaveable { mutableStateOf(false) }

    if (showCustomColor) {
        CustomColorDialog(uiState.keyColor,
            { actions.onSetKeyColor(it); showCustomColor = false },
            { showCustomColor = false }, strings)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = strings.themeSettings,
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
                // ── Phone preview ────────────────────────────────────────────
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ThemePreviewCard()
                }
                Spacer(Modifier.height(16.dp))

                // ── Seed color circles ───────────────────────────────────────
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    items(keyColorPresets) { opt ->
                        ColorCircle(opt.color, uiState.keyColor == opt.color) {
                            actions.onSetKeyColor(opt.color)
                        }
                    }
                    item {
                        // Custom "+" circle
                        Box(Modifier.size(48.dp).clip(CircleShape)
                            .background(colorScheme.secondary)
                            .clickable { showCustomColor = true },
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Add, null,
                                tint = colorScheme.onSecondary, modifier = Modifier.size(22.dp))
                        }
                    }
                }

                // ── 4 mode chips (Dynamic | Light | Dark | AMOLED) ───────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val isMonet = uiState.colorMode.isMonet ||
                            uiState.colorMode == ColorMode.MONET_SYSTEM
                    ColorModeChip(
                        icon     = Icons.Rounded.AutoAwesome,
                        selected = uiState.colorMode.isMonet,
                        modifier = Modifier.weight(1f),
                        onClick  = { actions.onSetColorMode(ColorMode.MONET_SYSTEM) },
                    )
                    ColorModeChip(
                        icon     = Icons.Rounded.LightMode,
                        selected = uiState.colorMode == ColorMode.LIGHT,
                        modifier = Modifier.weight(1f),
                        onClick  = { actions.onSetColorMode(ColorMode.LIGHT) },
                    )
                    ColorModeChip(
                        icon     = Icons.Rounded.DarkMode,
                        selected = uiState.colorMode == ColorMode.DARK,
                        modifier = Modifier.weight(1f),
                        onClick  = { actions.onSetColorMode(ColorMode.DARK) },
                    )
                    ColorModeChip(
                        icon     = Icons.Rounded.Brightness1,
                        selected = uiState.colorMode == ColorMode.DARK_AMOLED,
                        modifier = Modifier.weight(1f),
                        onClick  = { actions.onSetColorMode(ColorMode.DARK_AMOLED) },
                    )
                }
                Spacer(Modifier.height(12.dp))

                // ── Color style / spec (hide when Monet active) ──────────────
                if (!uiState.colorMode.isMonet) {
                    Card(modifier = Modifier.fillMaxWidth()) {
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
                                actions.onSetColorSpecVersion(
                                    if (it == 1) ColorSpec.SpecVersion.SPEC_2025 else ColorSpec.SpecVersion.SPEC_2021)
                            },
                            startAction = { PrefIcon(Icons.Rounded.Science) },
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // ── Layout ───────────────────────────────────────────────────
                Card(modifier = Modifier.fillMaxWidth()) {
                    val navLabels = listOf(strings.navAuto, strings.navBottom, strings.navRail)
                    OverlayDropdownPreference(
                        title   = strings.navBarStyle,
                        summary = navLabels[uiState.navLayoutMode.ordinal],
                        items   = navLabels,
                        selectedIndex = uiState.navLayoutMode.ordinal,
                        onSelectedIndexChange = { actions.onSetNavLayoutMode(NavLayoutMode.entries[it]) },
                        startAction = { PrefIcon(Icons.Rounded.ViewQuilt) },
                    )
                    SwitchPreference(
                        title   = strings.floatingBottomBar,
                        summary = strings.floatingBottomBarSummary,
                        checked = uiState.floatingBottomBar,
                        onCheckedChange = actions.onSetFloatingBottomBar,
                        startAction = { PrefIcon(Icons.Rounded.WebAsset) },
                    )
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
                Spacer(Modifier.height(12.dp))

                // ── Effects (Miuix-only) ─────────────────────────────────────
                Card(modifier = Modifier.fillMaxWidth()) {
                    SwitchPreference(
                        title   = strings.enableBlur,
                        summary = strings.enableBlurSummary,
                        checked = uiState.enableBlur,
                        onCheckedChange = actions.onSetEnableBlur,
                        startAction = { PrefIcon(Icons.Rounded.BlurOn) },
                    )
                    SwitchPreference(
                        title   = strings.smoothCorner,
                        summary = strings.smoothCornerSummary,
                        checked = uiState.enableSmoothCorner,
                        onCheckedChange = actions.onSetEnableSmoothCorner,
                        startAction = { PrefIcon(Icons.Rounded.RoundedCorner) },
                    )
                }
                Spacer(Modifier.height(12.dp))

                // ── Predictive back ──────────────────────────────────────────
                Card(modifier = Modifier.fillMaxWidth()) {
                    SwitchPreference(
                        title   = strings.predictiveBack,
                        summary = strings.predictiveBackSummary,
                        checked = uiState.predictiveBack,
                        onCheckedChange = actions.onSetPredictiveBack,
                        startAction = { PrefIcon(Icons.AutoMirrored.Rounded.ArrowBack) },
                    )
                }
                Spacer(Modifier.height(12.dp))

                // ── Interface scale (inline slider, like KernelSU) ───────────
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PrefIcon(Icons.Rounded.AspectRatio)
                            Column(Modifier.weight(1f)) {
                                Text(strings.pageScale,
                                    style = textStyles.main, color = colorScheme.onBackground)
                                Text(strings.pageScaleSummary,
                                    style = textStyles.body2, color = colorScheme.onSurfaceVariantSummary)
                            }
                            Text("${"%.0f".format(uiState.pageScale * 100)}%",
                                style = textStyles.body2, color = colorScheme.onSurfaceVariantSummary)
                        }
                        Slider(
                            value        = uiState.pageScale,
                            onValueChange= actions.onSetPageScale,
                            valueRange   = 0.6f..1.4f,
                            modifier     = Modifier.fillMaxWidth().padding(top = 4.dp),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ColorModeChip(
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bg    = if (selected) colorScheme.primary else colorScheme.secondaryContainer
    val tint  = if (selected) colorScheme.onPrimary else colorScheme.onSecondaryContainer
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun ColorCircle(color: Color, selected: Boolean, onClick: () -> Unit) =
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape).background(color)
            .border(3.dp, if (selected) colorScheme.primary else Color.Transparent, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Icon(Icons.Rounded.Check, null,
            tint = Color.White, modifier = Modifier.size(22.dp))
    }

@Composable
private fun PrefIcon(icon: ImageVector) =
    Icon(icon, null, Modifier.padding(end = 6.dp), colorScheme.onBackground)

@Composable
private fun CustomColorDialog(
    current: Color, onConfirm: (Color) -> Unit,
    onDismiss: () -> Unit, strings: AppStrings,
) {
    var hex by remember { mutableStateOf(
        (current.value.toLong() and 0xFFFFFF).toString(16).uppercase().padStart(6, '0')) }
    var isError by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { androidx.compose.material3.Text(strings.custom) },
        text    = {
            OutlinedTextField(
                value = hex, onValueChange = { hex = it.take(6).uppercase(); isError = false },
                label = { androidx.compose.material3.Text("#RRGGBB") }, isError = isError,
                singleLine = true,
                leadingIcon = { Box(Modifier.size(24.dp).clip(CircleShape).background(
                    runCatching { Color(("FF$hex").toLong(16)) }.getOrNull() ?: Color.Gray)) },
            )
        },
        confirmButton = { TextButton(onClick = {
            val c = runCatching { Color(("FF$hex").toLong(16)) }.getOrNull()
            if (c != null) onConfirm(c) else isError = true
        }) { androidx.compose.material3.Text(strings.ok) } },
        dismissButton = { TextButton(onClick = onDismiss) {
            androidx.compose.material3.Text(strings.cancel) } },
    )
}

private fun paletteStyleLabels(s: AppStrings) = listOf(
    s.paletteTonalSpot, s.paletteNeutral, s.paletteVibrant, s.paletteExpressive,
    s.paletteRainbow, s.paletteFruitSalad, s.paletteMonochrome, s.paletteFidelity, s.paletteContent,
)
