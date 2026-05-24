package org.cf0x.spicecompose.ui.screen.settings

import androidx.compose.runtime.Immutable
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.i18n.AppLanguage

@Immutable
data class SettingsUiState(
    val language: AppLanguage = AppLanguage.EN,
    val uiMode: UiMode        = UiMode.Miuix,
)

@Immutable
data class SettingsScreenActions(
    val onSetLanguage:    (AppLanguage) -> Unit,
    val onSetUiModeIndex: (Int) -> Unit,
    val onOpenTheme:      () -> Unit,
    val onOpenAbout:      () -> Unit,
)
