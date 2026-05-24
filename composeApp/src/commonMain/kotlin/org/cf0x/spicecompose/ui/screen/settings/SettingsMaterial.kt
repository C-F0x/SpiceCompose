package org.cf0x.spicecompose.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContactPage
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.i18n.AppLanguage
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings

@ExperimentalMaterial3Api
@Composable
fun SettingsPagerMaterial(
    uiState: SettingsUiState,
    actions: SettingsScreenActions,
) {
    val strings = LocalAppStrings.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = innerPadding,
        ) {
            // ── Language ──────────────────────────────────────────────────────
            item {
                var langExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = langExpanded,
                    onExpandedChange = { langExpanded = it },
                ) {
                    ListItem(
                        modifier = Modifier.menuAnchor(),
                        headlineContent = { Text(strings.language) },
                        supportingContent = { Text(uiState.language.displayName) },
                        leadingContent = { Icon(Icons.Rounded.Translate, null) },
                        trailingContent = { ExposedDropdownMenuDefaults.TrailingIcon(langExpanded) },
                    )
                    ExposedDropdownMenu(
                        expanded = langExpanded,
                        onDismissRequest = { langExpanded = false },
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang.displayName) },
                                onClick = {
                                    actions.onSetLanguage(lang)
                                    langExpanded = false
                                },
                            )
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // ── Appearance ────────────────────────────────────────────────────
            item {
                Text(
                    text = strings.appearance,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                )
            }
            item {
                var uiExpanded by remember { mutableStateOf(false) }
                val uiLabels = listOf("Miuix", "Material")
                ExposedDropdownMenuBox(
                    expanded = uiExpanded,
                    onExpandedChange = { uiExpanded = it },
                ) {
                    ListItem(
                        modifier = Modifier.menuAnchor(),
                        headlineContent = { Text(strings.uiStyle) },
                        supportingContent = { Text(strings.uiStyleSummary) },
                        leadingContent = { Icon(Icons.Rounded.Dashboard, null) },
                        trailingContent = {
                            ExposedDropdownMenuDefaults.TrailingIcon(uiExpanded)
                        },
                    )
                    ExposedDropdownMenu(
                        expanded = uiExpanded,
                        onDismissRequest = { uiExpanded = false },
                    ) {
                        uiLabels.forEachIndexed { i, label ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    actions.onSetUiModeIndex(i)
                                    uiExpanded = false
                                },
                            )
                        }
                    }
                }
            }
            item {
                ListItem(
                    modifier = Modifier.clickable(onClick = actions.onOpenTheme),
                    headlineContent = { Text(strings.themeSettings) },
                    supportingContent = { Text(strings.themeSettingsSummary) },
                    leadingContent = { Icon(Icons.Rounded.Palette, null) },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // ── About ─────────────────────────────────────────────────────────
            item {
                Text(
                    text = strings.about,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                )
            }
            item {
                ListItem(
                    modifier = Modifier.clickable(onClick = actions.onOpenAbout),
                    headlineContent = { Text(strings.about) },
                    leadingContent = { Icon(Icons.Rounded.ContactPage, null) },
                )
            }
        }
    }
}
