package org.cf0x.spicecompose.ui.screen.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.navigation.NavLayoutMode

/**
 * Live theme preview card.
 *
 * - Size: derived from actual screen dimensions (not hardcoded)
 * - Orientation: when screen is landscape the preview switches to a wide phone layout
 *   and the inner content rectangles flip their long axis accordingly
 * - NavLayout: side-rail preview when [navLayoutMode] == SideRail
 * - UiMode: Miuix vs Material layouts are correctly mapped
 */
@Composable
fun ThemePreviewCard(
    modifier: Modifier = Modifier,
    navLayoutMode: NavLayoutMode = NavLayoutMode.Auto,
) {
    // ── Screen dimensions via WindowInfo ─────────────────────────────────────
    val windowInfo  = LocalWindowInfo.current
    val screenW     = windowInfo.containerSize.width
    val screenH     = windowInfo.containerSize.height
    val isLandscape = screenW > screenH

    // Phone aspect ratio mirrors the actual device ratio (clamped to sane bounds)
    val phoneRatio = if (isLandscape) {
        // Landscape: preview is a wide phone (rotated 90°)
        (screenW.toFloat() / screenH).coerceIn(1.2f, 2.2f)
    } else {
        (screenH.toFloat() / screenW).coerceIn(1.6f, 2.4f)
    }

    // Preview occupies ~42% of the narrower screen dimension
    val previewShortSide: Dp = if (isLandscape) {
        (screenH * 0.42f / screenH.toFloat() * 100).dp.coerceIn(120.dp, 180.dp)
    } else {
        200.dp  // portrait: fixed readable width
    }
    val previewW = if (isLandscape) (previewShortSide.value * phoneRatio).dp else previewShortSide
    val previewH = if (isLandscape) previewShortSide else (previewShortSide.value * phoneRatio).dp

    // ── Colors ───────────────────────────────────────────────────────────────
    val bg      = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primaryContainer
    val surface = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outlineVariant
    val onBg    = MaterialTheme.colorScheme.onBackground
    val uiMode  = LocalUiMode.current

    // Resolve effective nav layout (Auto depends on screen width in dp)
    val useRail = when (navLayoutMode) {
        NavLayoutMode.SideRail  -> true
        NavLayoutMode.BottomBar -> false
        NavLayoutMode.Auto      -> screenW > screenH * 1.5f  // wide = rail
    }

    Box(
        modifier = modifier
            .width(previewW)
            .height(previewH)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.5.dp, outline, RoundedCornerShape(20.dp)),
    ) {
        when {
            useRail -> SideRailPreview(uiMode, isLandscape, primary, surface, onBg)
            else    -> BottomNavPreview(uiMode, isLandscape, primary, surface, onBg)
        }
    }
}

// ── Side rail layout ─────────────────────────────────────────────────────────
@Composable
private fun SideRailPreview(
    uiMode: UiMode, isLandscape: Boolean,
    primary: androidx.compose.ui.graphics.Color,
    surface: androidx.compose.ui.graphics.Color,
    onBg: androidx.compose.ui.graphics.Color,
) {
    Row(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        // Left rail strip
        Column(
            modifier = Modifier
                .width(18.dp).fillMaxHeight()
                .clip(RoundedCornerShape(8.dp)).background(surface),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(primary))
            repeat(3) { Box(Modifier.size(8.dp).clip(CircleShape).background(onBg.copy(alpha = .2f))) }
        }
        Spacer(Modifier.width(6.dp))
        // Content
        ContentArea(uiMode, isLandscape, primary, surface, onBg, Modifier.weight(1f).fillMaxHeight())
    }
}

// ── Bottom nav layout ────────────────────────────────────────────────────────
@Composable
private fun BottomNavPreview(
    uiMode: UiMode, isLandscape: Boolean,
    primary: androidx.compose.ui.graphics.Color,
    surface: androidx.compose.ui.graphics.Color,
    onBg: androidx.compose.ui.graphics.Color,
) {
    Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        ContentArea(uiMode, isLandscape, primary, surface, onBg, Modifier.weight(1f).fillMaxWidth())
        Spacer(Modifier.height(6.dp))
        // Nav bar
        when (uiMode) {
            UiMode.Material -> {
                // Material: wide pill or bottom bar with 4 dots
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.height(14.dp).width(28.dp).clip(RoundedCornerShape(7.dp)).background(primary))
                    repeat(3) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(surface))
                    }
                }
            }
            UiMode.Miuix -> {
                // Miuix: icons row
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Icon(Icons.Outlined.Home, null, tint = primary,
                        modifier = Modifier.size(12.dp))
                    repeat(3) {
                        Box(Modifier.size(12.dp).clip(RoundedCornerShape(4.dp))
                            .background(onBg.copy(alpha = .15f)))
                    }
                }
            }
        }
    }
}

// ── Inner content: SWAPPED from old version (Miuix=cards, Material=topbar) ──
@Composable
private fun ContentArea(
    uiMode: UiMode, isLandscape: Boolean,
    primary: androidx.compose.ui.graphics.Color,
    surface: androidx.compose.ui.graphics.Color,
    onBg: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // App label
        Text("SpiceCompose", fontSize = 7.sp, color = onBg,
            modifier = Modifier.padding(bottom = 4.dp))

        when (uiMode) {
            UiMode.Miuix -> {
                // Miuix style: card grid (tall card left + 2 right)
                // In landscape, flip long axis → top wide card + 2 below
                if (isLandscape) {
                    Box(Modifier.fillMaxWidth().height(20.dp)
                        .clip(RoundedCornerShape(6.dp)).background(primary))
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.weight(1f).fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp)).background(surface))
                        Box(Modifier.weight(1f).fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp)).background(surface))
                    }
                } else {
                    Row(Modifier.fillMaxWidth().height(54.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.weight(1.3f).fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp)).background(primary))
                        Column(Modifier.weight(1f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(Modifier.fillMaxWidth().weight(1f)
                                .clip(RoundedCornerShape(6.dp)).background(surface))
                            Box(Modifier.fillMaxWidth().weight(1f)
                                .clip(RoundedCornerShape(6.dp)).background(surface))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().weight(1f)
                        .clip(RoundedCornerShape(8.dp)).background(surface))
                }
            }

            UiMode.Material -> {
                // Material style: TopAppBar (full-width bar) + content
                // In landscape, the bar is shorter in height but still full-width
                val barH = if (isLandscape) 14.dp else 22.dp
                Box(Modifier.fillMaxWidth().height(barH)
                    .clip(RoundedCornerShape(6.dp)).background(primary))
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth().height(if (isLandscape) 12.dp else 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.weight(1f).fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp)).background(surface))
                    Box(Modifier.weight(1f).fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp)).background(surface))
                }
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth().weight(1f)
                    .clip(RoundedCornerShape(6.dp)).background(surface))
            }
        }
    }
}
