package org.cf0x.spicecompose.ui.i18n

import androidx.compose.runtime.Composable

/**
 * Platform-specific check: returns true when the OS has an active per-app
 * language override (Android 13+ Settings → App language).
 *
 * Desktop / Wasm always return false — no OS-level override exists.
 */
@Composable
expect fun isSystemLocaleOverridden(): Boolean
