package org.cf0x.spicecompose.ui.screen.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.rounded.Brightness1
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.ViewSidebar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import org.cf0x.spicecompose.ui.component.AdaptiveTopAppBar
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.component.TonalCard
import org.cf0x.spicecompose.ui.i18n.AppStrings
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.navigation.NavLayoutMode
import org.cf0x.spicecompose.ui.navigation.horizontalCutoutPadding
import org.cf0x.spicecompose.ui.theme.ColorMode
import org.cf0x.spicecompose.ui.theme.SpiceTheme
import org.cf0x.spicecompose.ui.theme.ThemePreferences

private val BlockSpacing = 12.dp

@ExperimentalMaterial3Api
@Composable
fun CustomizeScreenMaterial(uiState: CustomizeUiState, actions: CustomizeScreenActions) {
    val strings        = LocalAppStrings.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val fullscreen     = LocalFullscreenMode.current
    val p              = ThemePreferences
    val scope          = rememberCoroutineScope()

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
                AdaptiveTopAppBar(
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().horizontalCutoutPadding().nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(BlockSpacing)
        ) {
            // ── Top spacing ──────────────────────────────────────────────────
            item { Spacer(Modifier.height(BlockSpacing)) }

            // ── Preview ──────────────────────────────────────────────────────
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
                    shape = SpiceTheme.cornerShape(24.dp)
                ) {
                    Column {
                        if (uiState.colorMode != ColorMode.LIGHT) {
                            ListItem(
                                headlineContent = { Text(strings.amoledDark) },
                                supportingContent = { Text(strings.amoledDarkSummary) },
                                leadingContent = { Icon(Icons.Rounded.Brightness1, null) },
                                trailingContent = { Switch(uiState.amoledDark, actions.onSetAmoledDark) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }

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
                            DropdownMenu(expanded = paletteExpanded, onDismissRequest = { paletteExpanded = false }) {
                                paletteStyleLabels(strings).forEachIndexed { i, label ->
                                    DropdownMenuItem(text = { Text(label) }, onClick = {
                                        actions.onSetPaletteStyle(PaletteStyle.entries[i]); paletteExpanded = false
                                    })
                                }
                            }
                        }

                        ListItem(
                            modifier = Modifier.clickable { showAccentPicker = true },
                            headlineContent = { Text(strings.keyColor) },
                            supportingContent = { Text(strings.keyColorSummary) },
                            leadingContent = { Box(Modifier.size(24.dp).clip(CircleShape).background(uiState.keyColor)) },
                            trailingContent = { Icon(Icons.Rounded.ChevronRight, null) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        ExposedDropdownMenuBox(expanded = navExpanded, onExpandedChange = { navExpanded = it }) {
                            ListItem(
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                headlineContent = { Text(strings.navBarStyle) },
                                supportingContent = { Text(listOf(strings.navAuto, strings.navBottom, strings.navRail)[uiState.navLayoutMode.ordinal]) },
                                leadingContent = { Icon(Icons.AutoMirrored.Rounded.MenuOpen, null) },
                                trailingContent = { ExposedDropdownMenuDefaults.TrailingIcon(navExpanded) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                            DropdownMenu(expanded = navExpanded, onDismissRequest = { navExpanded = false }) {
                                listOf(strings.navAuto, strings.navBottom, strings.navRail).forEachIndexed { i, label ->
                                    DropdownMenuItem(text = { Text(label) }, onClick = { actions.onSetNavLayoutMode(NavLayoutMode.entries[i]); navExpanded = false })
                                }
                            }
                        }
                    }
                    if (uiState.navLayoutMode != NavLayoutMode.BottomBar) {
                        ListItem(
                            headlineContent = { Text(strings.sidebarLabels) },
                            supportingContent = { Text(strings.sidebarLabelsSummary) },
                            leadingContent = { Icon(Icons.Rounded.ViewSidebar, null) },
                            trailingContent = { Switch(ThemePreferences.sidebarExpanded, { ThemePreferences.updateSidebarExpanded(it) }) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }

            // ── Scale ────────────────────────────────────────────────────────
            item {
                TonalCard(modifier = Modifier.padding(horizontal = 16.dp), shape = SpiceTheme.cornerShape(24.dp)) {
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

            // ── Developer Mode ────────────────────────────────────────────
            item {
                TonalCard(
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp),
                    shape = SpiceTheme.cornerShape(24.dp)
                ) {
                    ListItem(
                        headlineContent = { Text(strings.devMode) },
                        supportingContent = { Text(strings.devModeSummary) },
                        leadingContent = { Icon(Icons.Rounded.Build, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Switch(p.devMode, p::updateDevMode) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            // ── Bottom spacing ───────────────────────────────────────────────
            item { Spacer(Modifier.height(BlockSpacing)) }
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
private fun ToggleCardMaterial(title: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    val strings = LocalAppStrings.current
    TonalCard(
        modifier = Modifier.fillMaxWidth(),
        shape = SpiceTheme.cornerShape(24.dp),
        onClick = { onToggle(!checked) }
    ) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(if (checked) strings.on else strings.off) },
            trailingContent = { Switch(checked = checked, onCheckedChange = onToggle) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

private fun paletteStyleLabels(s: AppStrings) = listOf(
    s.paletteTonalSpot, s.paletteNeutral, s.paletteVibrant, s.paletteExpressive,
    s.paletteRainbow, s.paletteFruitSalad, s.paletteMonochrome, s.paletteFidelity, s.paletteContent,
)
