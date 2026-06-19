package org.cf0x.spicecompose.ui.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.blur.Backdrop

@Composable
actual fun FloatingBottomBar(
    modifier: Modifier,
    selectedIndex: () -> Int,
    onSelected: (index: Int) -> Unit,
    tabsCount: Int,
    isBlurEnabled: Boolean,
    parentBackdrop: Backdrop?,
    content: @Composable RowScope.() -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        Row(
            Modifier.clip(CircleShape).background(
                top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surfaceContainer,
                CircleShape
            ).height(64.dp).padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}
