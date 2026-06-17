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
import androidx.compose.ui.unit.dp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import org.cf0x.spicecompose.ui.i18n.AppStrings
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.navigation.NavLayoutMode
import org.cf0x.spicecompose.ui.theme.ColorMode
import org.cf0x.spicecompose.ui.theme.defaultKeyColor
import org.cf0x.spicecompose.ui.theme.keyColorPresets
import org.cf0x.spicecompose.ui.theme.SpiceTheme
import org.cf0x.spicecompose.ui.component.TonalCard

@ExperimentalMaterial3Api
@Composable
fun ThemeScreenMaterial(uiState: ThemeUiState, actions: ThemeScreenActions) {
    val strings        = LocalAppStrings.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showAccentPicker by rememberSaveable { mutableStateOf(false) }
    var navExpanded      by rememberSaveable { mutableStateOf(false) }
    var paletteExpanded  by rememberSaveable { mutableStateOf(false) }
    var specExpanded     by rememberSaveable { mutableStateOf(false) }
    var localScale by remember(uiState.pageScale) { mutableFloatStateOf(uiState.pageScale) }

    if (showAccentPicker) {
        M3AccentColorDialog(
            current   = uiState.keyColor,
            onConfirm = { actions.onSetKeyColor(it); showAccentPicker = false },
            onDismiss = { showAccentPicker = false },
            strings   = strings,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.themeSettings) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = padding,
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
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Monet
                    TonalCard(shape = SpiceTheme.containerShape()) {
                        ListItem(
                            headlineContent = { Text(strings.monetEnable) },
                            supportingContent = { Text(strings.monetEnableSummary) },
                            leadingContent = { Icon(Icons.Rounded.Wallpaper, null) },
                            trailingContent = { Switch(uiState.useMonet, actions.onSetUseMonet) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }

                    // AMOLED
                    if (uiState.colorMode != ColorMode.LIGHT) {
                        TonalCard(shape = SpiceTheme.containerShape()) {
                            ListItem(
                                headlineContent = { Text(strings.amoledDark) },
                                supportingContent = { Text(strings.amoledDarkSummary) },
                                leadingContent = { Icon(Icons.Rounded.Brightness1, null) },
                                trailingContent = { Switch(uiState.amoledDark, actions.onSetAmoledDark) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }

                    // Accent Color
                    if (!uiState.useMonet) {
                        TonalCard(shape = SpiceTheme.containerShape(), onClick = { showAccentPicker = true }) {
                            ListItem(
                                headlineContent = { Text(strings.keyColor) },
                                supportingContent = { Text(strings.keyColorSummary) },
                                leadingContent = { Box(Modifier.size(24.dp).clip(CircleShape).background(uiState.keyColor)) },
                                trailingContent = { Icon(Icons.Rounded.ChevronRight, null) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }

                    // M3E Switch
                    TonalCard(shape = SpiceTheme.containerShape()) {
                        ListItem(
                            headlineContent = { Text(strings.m3e) },
                            supportingContent = { Text(strings.m3eSummary) },
                            leadingContent = { Icon(Icons.Rounded.AutoAwesome, null) },
                            trailingContent = { Switch(uiState.enableSmoothCorner, actions.onSetEnableSmoothCorner) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }

                    // Nav Style
                    ExposedDropdownMenuBox(expanded = navExpanded, onExpandedChange = { navExpanded = it }) {
                        TonalCard(modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable), shape = SpiceTheme.containerShape()) {
                            ListItem(
                                headlineContent = { Text(strings.navBarStyle) },
                                supportingContent = { Text(listOf(strings.navAuto, strings.navBottom, strings.navRail)[uiState.navLayoutMode.ordinal]) },
                                leadingContent = { Icon(Icons.AutoMirrored.Rounded.MenuOpen, null) },
                                trailingContent = { ExposedDropdownMenuDefaults.TrailingIcon(navExpanded) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                        ExposedDropdownMenu(expanded = navExpanded, onDismissRequest = { navExpanded = false }) {
                            listOf(strings.navAuto, strings.navBottom, strings.navRail).forEachIndexed { i, label ->
                                DropdownMenuItem(text = { Text(label) }, onClick = { actions.onSetNavLayoutMode(NavLayoutMode.entries[i]); navExpanded = false })
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
                            trailingContent = { Text("${"%.0f".format(localScale * 100)}%", color = MaterialTheme.colorScheme.primary) },
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
private fun M3AccentColorDialog(current: Color, onConfirm: (Color) -> Unit, onDismiss: () -> Unit, strings: AppStrings) {
    var hexInput by remember { mutableStateOf((current.value.toLong() and 0xFFFFFF).toString(16).uppercase().padStart(6, '0')) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.keyColor) },
        text = {
            androidx.compose.foundation.lazy.LazyColumn {
                item {
                    Row(Modifier.fillMaxWidth().clickable { onConfirm(defaultKeyColor) }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(32.dp).clip(CircleShape).background(defaultKeyColor)
                            .border(2.dp, if (current == defaultKeyColor) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape))
                        Text(strings.specDefault)
                    }
                }
                item {
                    keyColorPresets.chunked(5).forEach { row ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { opt ->
                                Box(Modifier.size(36.dp).clip(CircleShape).background(opt.color)
                                    .border(2.dp, if (current == opt.color) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                    .clickable { onConfirm(opt.color) }, contentAlignment = Alignment.Center) {
                                    if (current == opt.color) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = hexInput, onValueChange = { hexInput = it.take(6).uppercase(); isError = false },
                        label = { Text("#RRGGBB") }, isError = isError, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Box(Modifier.size(24.dp).clip(CircleShape).background(runCatching { Color(("FF$hexInput").toLong(16)) }.getOrNull() ?: Color.Gray)) },
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = {
            val c = runCatching { Color(("FF$hexInput").toLong(16)) }.getOrNull()
            if (c != null) onConfirm(c) else isError = true
        }) { Text(strings.ok) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}

private fun paletteStyleLabels(s: AppStrings) = listOf(
    s.paletteTonalSpot, s.paletteNeutral, s.paletteVibrant, s.paletteExpressive,
    s.paletteRainbow, s.paletteFruitSalad, s.paletteMonochrome, s.paletteFidelity, s.paletteContent,
)
