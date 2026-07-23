package org.cf0x.spicecompose.ui.screen.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContactPage
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.platform.maybeVibrate
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
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.theme.LocalEnableBlur
import org.cf0x.spicecompose.ui.util.BlurredBar
import org.cf0x.spicecompose.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

private val BlockSpacing = 12.dp

@Composable
fun SettingsPagerMiuix(
    uiState: SettingsUiState,
    actions: SettingsScreenActions,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur && LocalUiMode.current == UiMode.Miuix)
    val blurActive = backdrop != null
    val barColor = if (blurActive) Color.Transparent else colorScheme.surface
    val strings = LocalAppStrings.current

    Scaffold(
        topBar = {
            BlurredBar(backdrop, blurActive) {
                TopAppBar(
                    title = strings.settings,
                    actions = {},
                    color = barColor,
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        popupHost = {},
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)
                .padding(horizontal = BlockSpacing),
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding()),
            overscrollEffect = null,
        ) {
            // ── Top spacing ──────────────────────────────────────────────────
            item { Spacer(Modifier.height(BlockSpacing)) }

            // ── Language ─────────────────────────────────────────────────────
            item {
                val langEnabled = !uiState.systemLocaleOverridden
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    OverlayDropdownPreference(
                        title = strings.language,
                        summary = if (langEnabled) uiState.language.displayName
                                  else "${uiState.language.displayName} · ${strings.systemLocaleOverriddenHint}",
                        items = AppLanguage.entries.map { it.displayName },
                        selectedIndex = uiState.language.ordinal,
                        onSelectedIndexChange = {
                            if (langEnabled) {
                                maybeVibrate(15)
                                actions.onSetLanguage(AppLanguage.entries[it])
                            }
                        },
                        startAction = {
                            Icon(
                                imageVector = Icons.Rounded.Translate,
                                contentDescription = strings.language,
                                modifier = Modifier.padding(end = 6.dp),
                                tint = if (langEnabled) colorScheme.onBackground
                                       else colorScheme.onSurface.copy(alpha = 0.3f),
                            )
                        },
                    )
                }
            }

            // ── Appearance ───────────────────────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    OverlayDropdownPreference(
                        title = strings.uiStyle,
                        summary = strings.uiStyleSummary,
                        items = listOf(strings.styleMiuix, strings.styleMaterial),
                        selectedIndex = if (uiState.uiMode == UiMode.Material) 1 else 0,
                        onSelectedIndexChange = {
                            maybeVibrate(15)
                            actions.onSetUiModeIndex(it)
                        },
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
                        onClick = { maybeVibrate(15); actions.onOpenTheme() },
                    )
                }
            }

            // ── About ────────────────────────────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
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
                        onClick = { maybeVibrate(15); actions.onOpenAbout() },
                    )
                }
            }

            // ── Bottom spacing ───────────────────────────────────────────────
            item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
        }
    }
}
