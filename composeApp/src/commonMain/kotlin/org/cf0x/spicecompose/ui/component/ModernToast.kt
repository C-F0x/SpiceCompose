package org.cf0x.spicecompose.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.Surface as MiuixSurface
import top.yukonga.miuix.kmp.squircle.squircleBackground
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 自绘 Toast，Miuix / Material 双风格。
 * [text] 为 null 时不显示；非空时自动 2s 消失并回调 [onDismiss]。
 */
@Composable
fun ModernToast(
    text: String?,
    isMiuix: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visible = text != null

    LaunchedEffect(text) {
        if (text != null) {
            delay(2000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + scaleOut(targetScale = 0.9f),
        modifier = modifier,
    ) {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            val bottomPad = maxHeight * 0.10f
            if (isMiuix) {
                Box(
                    modifier = Modifier
                        .padding(bottom = bottomPad)
                        .squircleBackground(color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.85f), cornerRadius = 24.dp)
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    MiuixText(
                        text = text ?: "",
                        color = MiuixTheme.colorScheme.surface,
                        fontSize = 14.sp,
                    )
                }
            } else {
                androidx.compose.material3.Surface(
                    color = Color(0xE61C1B1F),
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier.padding(bottom = bottomPad),
                ) {
                    androidx.compose.material3.Text(
                        text = text ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}
