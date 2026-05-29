package org.cf0x.spicecompose.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

/**
 * Shared mutable flag: true when a sub-page (ThemeScreen, AboutScreen…) is active.
 * MainScreen reads this to disable HorizontalPager user-scroll.
 * SettingsScreen writes this via SideEffect.
 */
val LocalInSubPage = compositionLocalOf { mutableStateOf(false) }
