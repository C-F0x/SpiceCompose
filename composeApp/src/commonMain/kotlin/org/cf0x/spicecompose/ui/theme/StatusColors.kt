package org.cf0x.spicecompose.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 语义化颜色 token，供 Miuix 和 Material 模式共用。
 * 禁止在 screen 中散落 Color(0xFF...) —— 所有状态色统一经此引用。
 */
@Immutable
data class StatusColors(
    /** 连接成功（绿色系） */
    val connected: Color,
    /** 连接中 / 等待（黄色系） */
    val connecting: Color,
    /** 已断开 / 错误（灰色系） */
    val disconnected: Color,
    /** 危险操作（删除、关机） */
    val danger: Color,
    /** 正常 / 启用（绿色系） */
    val healthy: Color,
    /** 警告（黄色系） */
    val warning: Color,
    /** 中性 / 未知 */
    val neutral: Color,
    /** 已选中节点容器背景 */
    val selectedNodeContainer: Color,
    /** UI 模式切换 — 主模式 */
    val primaryMode: Color,
    /** UI 模式切换 — 次模式 */
    val secondaryMode: Color,
)

/** Monet（动态取色）下的语义色 */
val MonetStatusColors = StatusColors(
    connected = Color(0xFF36D167),
    connecting = Color(0xFFFFC107),
    disconnected = Color(0xFF888888),
    danger = Color(0xFFE53935),
    healthy = Color(0xFF36D167),
    warning = Color(0xFFFFC107),
    neutral = Color(0xFF888888),
    selectedNodeContainer = Color(0x1A36D167),
    primaryMode = Color(0xFF008080),
    secondaryMode = Color(0xFF800080),
)

/** 非 Monet（静态色）下的语义色 */
val StaticStatusColors = StatusColors(
    connected = Color(0xFF36D167),
    connecting = Color(0xFFFFC107),
    disconnected = Color(0xFF888888),
    danger = Color(0xFFE53935),
    healthy = Color(0xFF36D167),
    warning = Color(0xFFFFC107),
    neutral = Color(0xFF888888),
    selectedNodeContainer = Color(0x1A36D167),
    primaryMode = Color(0xFF008080),
    secondaryMode = Color(0xFF800080),
)

val LocalStatusColors = staticCompositionLocalOf { StaticStatusColors }

/** 根据当前 Monet 状态返回正确的 StatusColors */
@Composable
fun rememberStatusColors(): StatusColors {
    val isMonet = top.yukonga.miuix.kmp.theme.MiuixTheme.isDynamicColor
    return if (isMonet) MonetStatusColors else StaticStatusColors
}
