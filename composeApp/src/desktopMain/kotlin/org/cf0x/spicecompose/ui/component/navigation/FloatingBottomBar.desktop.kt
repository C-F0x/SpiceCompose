package org.cf0x.spicecompose.ui.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
actual fun FloatingBottomBar(
    modifier: Modifier,
    selectedIndex: () -> Int,
    onSelected: (index: Int) -> Unit,
    tabsCount: Int,
    isBlurEnabled: Boolean,
    content: @Composable RowScope.() -> Unit
) {
    val pillShape = CircleShape
    val surfaceContainer = MiuixTheme.colorScheme.surfaceContainer

    Box(
        modifier = modifier.width(IntrinsicSize.Min),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            Modifier
                .clip(pillShape)
                .background(surfaceContainer, pillShape)
                .height(64.dp)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}
