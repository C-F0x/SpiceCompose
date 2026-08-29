package org.cf0x.spicecompose.ui.i18n

import androidx.compose.runtime.Composable

/** iOS does not expose a per-app language override here. */
@Composable
actual fun isSystemLocaleOverridden(): Boolean = false
