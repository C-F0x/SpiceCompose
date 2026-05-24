package org.cf0x.spicecompose.ui.screen.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.LocalUiMode

/** Phone-shaped preview card that reflects the live theme colors. */
@Composable
fun ThemePreviewCard(modifier: Modifier = Modifier) {
    val bg        = MaterialTheme.colorScheme.background
    val primary   = MaterialTheme.colorScheme.primaryContainer
    val surface   = MaterialTheme.colorScheme.surfaceVariant
    val outline   = MaterialTheme.colorScheme.outlineVariant
    val onBg      = MaterialTheme.colorScheme.onBackground
    val navBg     = MaterialTheme.colorScheme.surfaceContainer
    val uiMode    = LocalUiMode.current

    Box(
        modifier = modifier
            .width(200.dp)
            .height(290.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .border(1.5.dp, outline, RoundedCornerShape(24.dp)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            // App name
            Text("SpiceCompose", fontSize = 9.sp, color = onBg,
                modifier = Modifier.padding(bottom = 6.dp))

            if (uiMode == UiMode.Miuix) {
                // Miuix layout: full-width card, then 2 small cards
                Box(Modifier.fillMaxWidth().height(32.dp).clip(RoundedCornerShape(8.dp)).background(primary))
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.weight(1f).height(20.dp).clip(RoundedCornerShape(6.dp)).background(surface))
                    Box(Modifier.weight(1f).height(20.dp).clip(RoundedCornerShape(6.dp)).background(surface))
                }
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(8.dp)).background(surface))
                Spacer(Modifier.height(8.dp))
                // Miuix nav: single home icon centered
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Home, null, tint = onBg.copy(alpha = .6f),
                        modifier = Modifier.size(16.dp))
                }
            } else {
                // Material layout: left tall card + right 2 small cards
                Row(Modifier.fillMaxWidth().height(80.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.weight(1.2f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(primary))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(6.dp)).background(surface))
                        Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(6.dp)).background(surface))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(8.dp)).background(surface))
                Spacer(Modifier.height(8.dp))
                // M3 nav: 4 dots
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    repeat(4) { i ->
                        Box(Modifier.size(if (i == 0) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (i == 0) primary else surface))
                    }
                }
            }
        }
    }
}
