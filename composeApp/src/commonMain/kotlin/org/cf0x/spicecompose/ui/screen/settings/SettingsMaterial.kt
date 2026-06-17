package org.cf0x.spicecompose.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.i18n.AppLanguage
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.component.TonalCard
import org.cf0x.spicecompose.ui.theme.SpiceTheme

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
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 16.dp),
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

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
                            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                        )
                    }
                    ExposedDropdownMenu(
                        expanded = langExpanded,
                        onDismissRequest = { langExpanded = false }
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang.displayName) },
                                onClick = {
                                    actions.onSetLanguage(lang)
                                    langExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // ── UI Style (Pop-up Dialog) ─────────────────────────────────────
            item {
                TonalCard(shape = SpiceTheme.containerShape(), onClick = { uiModeDialog = true }) {
                    ListItem(
                        headlineContent = { Text(strings.uiStyle) },
                        supportingContent = { Text(if (uiState.uiMode == UiMode.Miuix) "Miuix" else "Material You") },
                        leadingContent = { Icon(Icons.Rounded.Style, null) },
                        trailingContent = { Icon(Icons.Rounded.ChevronRight, null) },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                    )
                }
            }

            // ── Theme Settings ───────────────────────────────────────────────
            item {
                TonalCard(shape = SpiceTheme.containerShape(), onClick = actions.onOpenTheme) {
                    ListItem(
                        headlineContent = { Text(strings.themeSettings) },
                        supportingContent = { Text(strings.themeSettingsSummary) },
                        leadingContent = { Icon(Icons.Rounded.Palette, null) },
                        trailingContent = { Icon(Icons.Rounded.ChevronRight, null) },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                    )
                }
            }

            // ── About ────────────────────────────────────────────────────────
            item {
                TonalCard(shape = SpiceTheme.containerShape(), onClick = actions.onOpenAbout) {
                    ListItem(
                        headlineContent = { Text(strings.about) },
                        supportingContent = { Text("Version info and more") },
                        leadingContent = { Icon(Icons.Rounded.Info, null) },
                        trailingContent = { Icon(Icons.Rounded.ChevronRight, null) },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                    )
                }
            }
            
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (uiModeDialog) {
        AlertDialog(
            onDismissRequest = { uiModeDialog = false },
            title = { Text(strings.uiStyle) },
            text = {
                Column {
                    ListItem(
                        modifier = Modifier.clickable { actions.onSetUiModeIndex(0); uiModeDialog = false },
                        headlineContent = { Text("Miuix") },
                        leadingContent = { RadioButton(selected = uiState.uiMode == UiMode.Miuix, onClick = null) },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                    )
                    ListItem(
                        modifier = Modifier.clickable { actions.onSetUiModeIndex(1); uiModeDialog = false },
                        headlineContent = { Text("Material You") },
                        leadingContent = { RadioButton(selected = uiState.uiMode == UiMode.Material, onClick = null) },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                    )
                }
            },
            confirmButton = { TextButton(onClick = { uiModeDialog = false }) { Text(strings.cancel) } }
        )
    }
}
