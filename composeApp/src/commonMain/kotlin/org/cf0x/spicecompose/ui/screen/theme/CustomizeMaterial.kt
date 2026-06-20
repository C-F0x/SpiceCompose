package org.cf0x.spicecompose.ui.screen.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import org.cf0x.spicecompose.ui.i18n.AppStrings
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.navigation.NavLayoutMode
import org.cf0x.spicecompose.ui.theme.ColorMode
import org.cf0x.spicecompose.ui.theme.defaultKeyColor
import org.cf0x.spicecompose.ui.theme.keyColorPresets
import org.cf0x.spicecompose.ui.theme.rememberSystemAccentColor
import org.cf0x.spicecompose.ui.theme.SpiceTheme
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.component.TonalCard
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.platform.maybeVibrate
import org.cf0x.spicecompose.platform.vibrationAvailable
import org.cf0x.spicecompose.ui.theme.ThemePreferences

@ExperimentalMaterial3Api
@Composable
fun CustomizeScreenMaterial(uiState: CustomizeUiState, actions: CustomizeScreenActions) {
    val strings        = LocalAppStrings.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val fullscreen = LocalFullscreenMode.current
    val p = ThemePreferences
    val scope = rememberCoroutineScope()

    SpiceBackHandler(enabled = fullscreen.value) {
        fullscreen.value = false
    }

    var showAccentPicker by rememberSaveable { mutableStateOf(false) }
    var navExpanded      by rememberSaveable { mutableStateOf(false) }
    var paletteExpanded  by rememberSaveable { mutableStateOf(false) }
    var specExpanded     by rememberSaveable { mutableStateOf(false) }
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
                    title = { Text(strings.themeSettings) },
                    navigationIcon = {
                        IconButton(onClick = actions.onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                        }
                    },
                    actions = {
                        FullscreenAction()
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
    ) { padding ->
        val contentPadding = if (fullscreen.value) PaddingValues(0.dp) else padding
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // ── Preview ──────────────────────────────────────────────────────
            item {
                Box(Modifier.fillMaxWidth().padding(bottom = 12.dp), contentAlignment = Alignment.Center) {
                    ThemePreviewCard(navLayoutMode = uiState.navLayoutMode)
                }
            }

            // ── Mode Chips ───────────────────────────────────────────────────
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    M3ModeChip(Icons.Rounded.Star,      uiState.colorMode == ColorMode.SYSTEM, Modifier.weight(1f)) { actions.onSetColorMode(ColorMode.SYSTEM) }
                    M3ModeChip(Icons.Rounded.LightMode, uiState.colorMode == ColorMode.LIGHT,  Modifier.weight(1f)) { actions.onSetColorMode(ColorMode.LIGHT) }
                    M3ModeChip(Icons.Rounded.DarkMode,  uiState.colorMode == ColorMode.DARK,   Modifier.weight(1f)) { actions.onSetColorMode(ColorMode.DARK) }
                }
            }

            // ── Color & Layout Section ───────────────────────────────────────
            item {
                TonalCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    shape = SpiceTheme.containerShape()
                ) {
                    Column {
                        // AMOLED
                        if (uiState.colorMode != ColorMode.LIGHT) {
                            ListItem(
                                headlineContent = { Text(strings.amoledDark) },
                                supportingContent = { Text(strings.amoledDarkSummary) },
                                leadingContent = { Icon(Icons.Rounded.Brightness1, null) },
                                trailingContent = { Switch(uiState.amoledDark, actions.onSetAmoledDark) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }

                        // Palette Style
                        ExposedDropdownMenuBox(expanded = paletteExpanded, onExpandedChange = { paletteExpanded = it }) {
                            ListItem(
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                headlineContent = { Text(strings.paletteStyle) },
                                supportingContent = {
                                    val labels = paletteStyleLabels(strings)
                                    Text(labels[PaletteStyle.entries.indexOf(uiState.paletteStyle).coerceAtLeast(0)])
                                },
                                leadingContent = { Icon(Icons.Rounded.ColorLens, null) },
                                trailingContent = { ExposedDropdownMenuDefaults.TrailingIcon(paletteExpanded) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                            ExposedDropdownMenu(expanded = paletteExpanded, onDismissRequest = { paletteExpanded = false }) {
                                paletteStyleLabels(strings).forEachIndexed { i, label ->
                                    DropdownMenuItem(text = { Text(label) }, onClick = {
                                        actions.onSetPaletteStyle(PaletteStyle.entries[i]); paletteExpanded = false
                                    })
                                }
                            }
                        }

                        // Accent Color
                        ListItem(
                            modifier = Modifier.clickable { showAccentPicker = true },
                            headlineContent = { Text(strings.keyColor) },
                            supportingContent = { Text(strings.keyColorSummary) },
                            leadingContent = { Box(Modifier.size(24.dp).clip(CircleShape).background(uiState.keyColor)) },
                            trailingContent = { Icon(Icons.Rounded.ChevronRight, null) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        // Color Spec
                        ExposedDropdownMenuBox(expanded = specExpanded, onExpandedChange = { specExpanded = it }) {
                            ListItem(
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                headlineContent = { Text(strings.colorSpec) },
                                supportingContent = {
                                    Text(if (uiState.colorSpecVersion == ColorSpec.SpecVersion.SPEC_2025) strings.spec2025 else strings.spec2021)
                                },
                                leadingContent = { Icon(Icons.Rounded.Science, null) },
                                trailingContent = { ExposedDropdownMenuDefaults.TrailingIcon(specExpanded) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                            ExposedDropdownMenu(expanded = specExpanded, onDismissRequest = { specExpanded = false }) {
                                listOf(strings.spec2021, strings.spec2025).forEachIndexed { i, label ->
                                    DropdownMenuItem(text = { Text(label) }, onClick = {
                                        actions.onSetColorSpecVersion(if (i == 1) ColorSpec.SpecVersion.SPEC_2025 else ColorSpec.SpecVersion.SPEC_2021)
                                        specExpanded = false
                                    })
                                }
                            }
                        }

                        // M3E Switch
                        ListItem(
                            headlineContent = { Text(strings.m3e) },
                            supportingContent = { Text(strings.m3eSummary) },
                            leadingContent = { Icon(Icons.Rounded.AutoAwesome, null) },
                            trailingContent = { Switch(uiState.enableSmoothCorner, actions.onSetEnableSmoothCorner) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        // Nav Style
                        ExposedDropdownMenuBox(expanded = navExpanded, onExpandedChange = { navExpanded = it }) {
                            ListItem(
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                headlineContent = { Text(strings.navBarStyle) },
                                supportingContent = { Text(listOf(strings.navAuto, strings.navBottom, strings.navRail)[uiState.navLayoutMode.ordinal]) },
                                leadingContent = { Icon(Icons.AutoMirrored.Rounded.MenuOpen, null) },
                                trailingContent = { ExposedDropdownMenuDefaults.TrailingIcon(navExpanded) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                            ExposedDropdownMenu(expanded = navExpanded, onDismissRequest = { navExpanded = false }) {
                                listOf(strings.navAuto, strings.navBottom, strings.navRail).forEachIndexed { i, label ->
                                    DropdownMenuItem(text = { Text(label) }, onClick = { actions.onSetNavLayoutMode(NavLayoutMode.entries[i]); navExpanded = false })
                                }
                            }
                        }
                    }
                }
            }

            // ── Scale ────────────────────────────────────────────────────────
            item {
                TonalCard(modifier = Modifier.padding(horizontal = 16.dp), shape = SpiceTheme.containerShape()) {
                    Column(Modifier.padding(16.dp)) {
                        ListItem(
                            headlineContent = { Text(strings.pageScale) },
                            supportingContent = { Text(strings.pageScaleSummary) },
                            trailingContent = { Text("${(localScale * 100).toInt()}%", color = MaterialTheme.colorScheme.primary) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                        Slider(
                            value = localScale,
                            onValueChange = { localScale = it },
                            onValueChangeFinished = { actions.onSetPageScale(localScale) },
                            valueRange = 0.6f..1.4f,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        )
                    }
                }
            }
            
            item { Spacer(Modifier.height(24.dp)) }
            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TonalCard(shape = SpiceTheme.cornerShape(24.dp)) {
                        Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                            ListItem(
                                headlineContent = { Text("Vibration") },
                                supportingContent = { Text(if (p.vibrationEnabled) "On" else "Off") },
                                leadingContent = { Icon(Icons.Rounded.Vibration, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary) },
                                trailingContent = { Switch(p.vibrationEnabled, p::updateVibrationEnabled) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                            if (vibrationAvailable && p.vibrationEnabled) {
                                Text("Duration: ${p.vibDuration}ms", style = MaterialTheme.typography.bodySmall)
                                Slider(value = p.vibDuration.toFloat(), onValueChange = { p.updateVibDuration(it.toInt()) }, valueRange = 0f..200f, steps = 19)
                                TextButton(onClick = { scope.launch { maybeVibrate(p.vibDuration.toLong()) } }) { Text("Test") }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun M3ModeChip(icon: ImageVector, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier.height(56.dp).clip(CircleShape).background(bg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp)) }
}

@Composable
private fun VibrationCardMaterial() {
    val scope = rememberCoroutineScope()
    val p = ThemePreferences
    val enabled = vibrationAvailable
    TonalCard(
        modifier = Modifier.fillMaxWidth(),
        shape = SpiceTheme.cornerShape(24.dp)
    ) {
        Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Vibration, null, Modifier.size(20.dp),
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                Spacer(Modifier.width(12.dp))
                Text(if (enabled) "Vibration Test" else "Vibration (Not supported)", style = MaterialTheme.typography.titleMedium)
            }
            if (enabled) {
                Spacer(Modifier.height(8.dp))
                Text("Duration: ${p.vibDuration}ms", style = MaterialTheme.typography.bodySmall)
                Slider(value = p.vibDuration.toFloat(), onValueChange = { p.updateVibDuration(it.toInt()) }, valueRange = 0f..200f, steps = 19)
                TextButton(onClick = { scope.launch { maybeVibrate(p.vibDuration.toLong()) } }) { Text("Test") }
            }
        }
    }
}

@Composable
private fun ToggleCardMaterial(title: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    TonalCard(
        modifier = Modifier.fillMaxWidth(),
        shape = SpiceTheme.cornerShape(24.dp),
        onClick = { onToggle(!checked) }
    ) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(if (checked) "On" else "Off") },
            trailingContent = { Switch(checked = checked, onCheckedChange = onToggle) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

private fun paletteStyleLabels(s: AppStrings) = listOf(
    s.paletteTonalSpot, s.paletteNeutral, s.paletteVibrant, s.paletteExpressive,
    s.paletteRainbow, s.paletteFruitSalad, s.paletteMonochrome, s.paletteFidelity, s.paletteContent,
)
