package org.cf0x.spicecompose.ui.screen.settings

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContactPage
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.i18n.AppLanguage
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun SettingsPagerMiuix(
    uiState: SettingsUiState,
    actions: SettingsScreenActions,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val strings = LocalAppStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = strings.settings,
                scrollBehavior = scrollBehavior,
            )
        },
        popupHost = {},
    ) { innerPadding ->
        LazyColumn(
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
                // ── Language (top-level, solo card) ──────────────────────────
                Card(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                ) {
                    OverlayDropdownPreference(
                        title = strings.language,
                        summary = uiState.language.displayName,
                        items = AppLanguage.entries.map { it.displayName },
                        selectedIndex = uiState.language.ordinal,
                        onSelectedIndexChange = { actions.onSetLanguage(AppLanguage.entries[it]) },
                        startAction = {
                            Icon(
                                imageVector = Icons.Rounded.Translate,
                                contentDescription = strings.language,
                                modifier = Modifier.padding(end = 6.dp),
                                tint = colorScheme.onBackground,
                            )
                        },
                    )
                }

                // ── Appearance ────────────────────────────────────────────────
                Card(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                ) {
                    OverlayDropdownPreference(
                        title = strings.uiStyle,
                        summary = strings.uiStyleSummary,
                        items = listOf("Miuix", "Material"),
                        selectedIndex = if (uiState.uiMode == UiMode.Material) 1 else 0,
                        onSelectedIndexChange = actions.onSetUiModeIndex,
                        startAction = {
                            Icon(
                                imageVector = Icons.Rounded.Dashboard,
                                contentDescription = strings.uiStyle,
                                modifier = Modifier.padding(end = 6.dp),
                                tint = colorScheme.onBackground,
                            )
                        },
                    )
                    ArrowPreference(
                        title = strings.themeSettings,
                        summary = strings.themeSettingsSummary,
                        startAction = {
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = strings.themeSettings,
                                modifier = Modifier.padding(end = 6.dp),
                                tint = colorScheme.onBackground,
                            )
                        },
                        onClick = actions.onOpenTheme,
                    )
                }

                // ── About ─────────────────────────────────────────────────────
                Card(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 12.dp)
                        .fillMaxWidth(),
                ) {
                    ArrowPreference(
                        title = strings.about,
                        startAction = {
                            Icon(
                                imageVector = Icons.Rounded.ContactPage,
                                contentDescription = strings.about,
                                modifier = Modifier.padding(end = 6.dp),
                                tint = colorScheme.onBackground,
                            )
                        },
                        onClick = actions.onOpenAbout,
                    )
                }
            }
        }
    }
}
