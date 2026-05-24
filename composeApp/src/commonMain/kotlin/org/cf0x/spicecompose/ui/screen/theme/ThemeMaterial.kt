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
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import org.cf0x.spicecompose.ui.i18n.AppStrings
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.navigation.NavLayoutMode
import org.cf0x.spicecompose.ui.theme.ColorMode
import org.cf0x.spicecompose.ui.theme.keyColorPresets

/**
 * Material theme settings screen.
 * Layout mirrors KernelSU's Material implementation:
 *   - Phone preview mockup
 *   - 3 text segment tabs  (System | Light | Dark)
 *   - Enable Monet toggle
 *   - Accent color row  (only when Monet is OFF)
 *   - Blur toggle
 *   - Floating bar toggle
 *   - Predictive back toggle
 *   - Interface scale (list item value + inline slider)
 */
@ExperimentalMaterial3Api
@Composable
fun ThemeScreenMaterial(uiState: ThemeUiState, actions: ThemeScreenActions) {
    val strings = LocalAppStrings.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showCustomColor by rememberSaveable { mutableStateOf(false) }
    var navExpanded     by rememberSaveable { mutableStateOf(false) }

    if (showCustomColor) {
        M3CustomColorDialog(uiState.keyColor,
            { actions.onSetKeyColor(it); showCustomColor = false },
            { showCustomColor = false }, strings)
    }

    // ── Derive tab + monet from ColorMode ────────────────────────────────────
    val baseTab = when {
        uiState.colorMode.isLight -> 1
        uiState.colorMode.isDark || uiState.colorMode.isAmoled -> 2
        else -> 0
    }
    val monetEnabled = uiState.colorMode.isMonet

    fun resolveMode(tab: Int, monet: Boolean): ColorMode = when {
        monet && tab == 0 -> ColorMode.MONET_SYSTEM
        monet && tab == 1 -> ColorMode.MONET_LIGHT
        monet && tab == 2 -> ColorMode.MONET_DARK
        tab == 1          -> ColorMode.LIGHT
        tab == 2          -> ColorMode.DARK
        else              -> ColorMode.SYSTEM
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
        ) {
            // ── Phone preview ────────────────────────────────────────────────
            item {
                Box(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
                    contentAlignment = Alignment.Center) {
                    ThemePreviewCard()
                }
            }

            // ── 3-tab mode selector ──────────────────────────────────────────
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                ) {
                    val labels = listOf(strings.colorModeSystem, strings.colorModeLight, strings.colorModeDark)
                    labels.forEachIndexed { idx, label ->
                        SegmentedButton(
                            selected = baseTab == idx,
                            onClick  = { actions.onSetColorMode(resolveMode(idx, monetEnabled)) },
                            shape    = SegmentedButtonDefaults.itemShape(idx, labels.size),
                            label    = { Text(label) },
                        )
                    }
                }
            }

            // ── Enable Monet ─────────────────────────────────────────────────
            item {
                ListItem(
                    headlineContent   = { Text(strings.monetEnable) },
                    supportingContent = { Text(strings.monetEnableSummary) },
                    leadingContent    = { Icon(Icons.Rounded.Wallpaper, null) },
                    trailingContent   = {
                        Switch(
                            checked         = monetEnabled,
                            onCheckedChange = { actions.onSetColorMode(resolveMode(baseTab, it)) },
                        )
                    },
                )
                HorizontalDivider()
            }

            // ── Accent color (only when Monet is OFF) ────────────────────────
            if (!monetEnabled) {
                item {
                    ListItem(
                        headlineContent   = { Text(strings.keyColor) },
                        supportingContent = { Text(strings.keyColorSummary) },
                        leadingContent    = { Icon(Icons.Rounded.Palette, null) },
                    )
                    LazyRow(
                        modifier = Modifier.padding(start = 72.dp, end = 16.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(keyColorPresets) { opt ->
                            val sel = uiState.keyColor == opt.color
                            Box(Modifier.size(40.dp).clip(CircleShape).background(opt.color)
                                .border(2.dp,
                                    if (sel) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    CircleShape)
                                .clickable { actions.onSetKeyColor(opt.color) },
                                contentAlignment = Alignment.Center) {
                                if (sel) Icon(Icons.Rounded.Check, null, tint = Color.White,
                                    modifier = Modifier.size(18.dp))
                            }
                        }
                        item {
                            Box(Modifier.size(40.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable { showCustomColor = true },
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Add, null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                item { HorizontalDivider() }
            }

            // ── Blur ─────────────────────────────────────────────────────────
            item {
                ListItem(
                    headlineContent   = { Text(strings.enableBlur) },
                    supportingContent = { Text(strings.enableBlurSummary) },
                    leadingContent    = { Icon(Icons.Rounded.BlurOn, null) },
                    trailingContent   = { Switch(uiState.enableBlur, actions.onSetEnableBlur) },
                )
                HorizontalDivider()
            }

            // ── Floating bottom bar ──────────────────────────────────────────
            item {
                ListItem(
                    headlineContent   = { Text(strings.floatingBottomBar) },
                    supportingContent = { Text(strings.floatingBottomBarSummary) },
                    leadingContent    = { Icon(Icons.Rounded.WebAsset, null) },
                    trailingContent   = { Switch(uiState.floatingBottomBar, actions.onSetFloatingBottomBar) },
                )
                HorizontalDivider()
            }

            // ── Predictive back ──────────────────────────────────────────────
            item {
                ListItem(
                    headlineContent   = { Text(strings.predictiveBack) },
                    supportingContent = { Text(strings.predictiveBackSummary) },
                    leadingContent    = { Icon(Icons.AutoMirrored.Rounded.ArrowBackIos, null) },
                    trailingContent   = { Switch(uiState.predictiveBack, actions.onSetPredictiveBack) },
                )
                HorizontalDivider()
            }

            // ── Interface scale (inline slider) ──────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    ListItem(
                        headlineContent   = { Text(strings.pageScale) },
                        supportingContent = { Text(strings.pageScaleSummary) },
                        leadingContent    = { Icon(Icons.Rounded.AspectRatio, null) },
                        trailingContent   = {
                            Text("${"%.0f".format(uiState.pageScale * 100)}%",
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        modifier = Modifier.padding(0.dp),
                    )
                    Slider(
                        value         = uiState.pageScale,
                        onValueChange = actions.onSetPageScale,
                        valueRange    = 0.6f..1.4f,
                        modifier      = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    )
                }
            }

            // ── Nav style (universal) ────────────────────────────────────────
            item {
                ExposedDropdownMenuBox(expanded = navExpanded, onExpandedChange = { navExpanded = it }) {
                    ListItem(
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        headlineContent   = { Text(strings.navBarStyle) },
                        supportingContent = {
                            Text(listOf(strings.navAuto, strings.navBottom, strings.navRail)[uiState.navLayoutMode.ordinal])
                        },
                        leadingContent    = { Icon(Icons.AutoMirrored.Rounded.MenuOpen, null) },
                        trailingContent   = { ExposedDropdownMenuDefaults.TrailingIcon(navExpanded) },
                    )
                    ExposedDropdownMenu(expanded = navExpanded, onDismissRequest = { navExpanded = false }) {
                        listOf(strings.navAuto, strings.navBottom, strings.navRail).forEachIndexed { i, label ->
                            DropdownMenuItem(text = { Text(label) }, onClick = {
                                actions.onSetNavLayoutMode(NavLayoutMode.entries[i]); navExpanded = false
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun M3CustomColorDialog(
    current: Color, onConfirm: (Color) -> Unit,
    onDismiss: () -> Unit, strings: AppStrings,
) {
    var hex by remember { mutableStateOf(
        (current.value.toLong() and 0xFFFFFF).toString(16).uppercase().padStart(6, '0')) }
    var isError by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(strings.custom) },
        text = {
            OutlinedTextField(
                value = hex, onValueChange = { hex = it.take(6).uppercase(); isError = false },
                label = { Text("#RRGGBB") }, isError = isError, singleLine = true,
                leadingIcon = { Box(Modifier.size(24.dp).clip(CircleShape).background(
                    runCatching { Color(("FF$hex").toLong(16)) }.getOrNull() ?: Color.Gray)) })
        },
        confirmButton = { TextButton(onClick = {
            val c = runCatching { Color(("FF$hex").toLong(16)) }.getOrNull()
            if (c != null) onConfirm(c) else isError = true
        }) { Text(strings.ok) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } })
}
