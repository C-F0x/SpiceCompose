package org.cf0x.spicecompose.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.platform.maybeVibrate
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.component.TonalCard
import org.cf0x.spicecompose.ui.i18n.AppLanguage
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.theme.SpiceTheme

private val BlockSpacing = 12.dp
private val HorizontalPadding = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPagerMaterial(
    uiState: SettingsUiState,
    actions: SettingsScreenActions,
) {
    val strings = LocalAppStrings.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var langExpanded by remember { mutableStateOf(false) }
    var uiModeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settings) },
                actions = {},
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = HorizontalPadding),
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(BlockSpacing)
        ) {
            // ── Top spacing ──────────────────────────────────────────────────
            item { Spacer(Modifier.height(BlockSpacing)) }

            // ── Language ─────────────────────────────────────────────────────
            item {
                ExposedDropdownMenuBox(
                    expanded = langExpanded,
                    onExpandedChange = { langExpanded = it }
                ) {
                    TonalCard(
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = SpiceTheme.containerShape()
                    ) {
                        ListItem(
                            headlineContent = { Text(strings.language) },
                            supportingContent = { Text(uiState.language.displayName) },
                            leadingContent = { Icon(Icons.Rounded.Translate, null) },
                            trailingContent = { ExposedDropdownMenuDefaults.TrailingIcon(langExpanded) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                    DropdownMenu(
                        expanded = langExpanded,
                        onDismissRequest = { langExpanded = false }
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang.displayName) },
                                onClick = {
                                    maybeVibrate(15)
                                    actions.onSetLanguage(lang)
                                    langExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // ── UI Style ─────────────────────────────────────────────────────
            item {
                TonalCard(
                    shape = SpiceTheme.containerShape(),
                    onClick = { maybeVibrate(15); uiModeDialog = true }
                ) {
                    ListItem(
                        headlineContent = { Text(strings.uiStyle) },
                        supportingContent = { Text(if (uiState.uiMode == UiMode.Miuix) "Miuix" else "Material You") },
                        leadingContent = { Icon(Icons.Rounded.Style, null) },
                        trailingContent = { Icon(Icons.Rounded.ChevronRight, null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            // ── Theme Settings ────────────────────────────────────────────────
            item {
                TonalCard(
                    shape = SpiceTheme.containerShape(),
                    onClick = { maybeVibrate(15); actions.onOpenTheme() }
                ) {
                    ListItem(
                        headlineContent = { Text(strings.themeSettings) },
                        supportingContent = { Text(strings.themeSettingsSummary) },
                        leadingContent = { Icon(Icons.Rounded.Palette, null) },
                        trailingContent = { Icon(Icons.Rounded.ChevronRight, null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            // ── About ────────────────────────────────────────────────────────
            item {
                TonalCard(
                    shape = SpiceTheme.containerShape(),
                    onClick = { maybeVibrate(15); actions.onOpenAbout() }
                ) {
                    ListItem(
                        headlineContent = { Text(strings.about) },
                        supportingContent = { Text("Version info and more") },
                        leadingContent = { Icon(Icons.Rounded.Info, null) },
                        trailingContent = { Icon(Icons.Rounded.ChevronRight, null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            // ── Bottom spacing ───────────────────────────────────────────────
            item { Spacer(Modifier.height(BlockSpacing)) }
        }
    }

    if (uiModeDialog) {
        AlertDialog(
            onDismissRequest = { uiModeDialog = false },
            title = { Text(strings.uiStyle) },
            text = {
                Column {
                    ListItem(
                        modifier = Modifier.clickable {
                            maybeVibrate(15)
                            actions.onSetUiModeIndex(0)
                            uiModeDialog = false
                        },
                        headlineContent = { Text("Miuix") },
                        leadingContent = { RadioButton(selected = uiState.uiMode == UiMode.Miuix, onClick = null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    ListItem(
                        modifier = Modifier.clickable {
                            maybeVibrate(15)
                            actions.onSetUiModeIndex(1)
                            uiModeDialog = false
                        },
                        headlineContent = { Text("Material You") },
                        leadingContent = { RadioButton(selected = uiState.uiMode == UiMode.Material, onClick = null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            },
            confirmButton = { TextButton(onClick = { uiModeDialog = false }) { Text(strings.cancel) } }
        )
    }
}
