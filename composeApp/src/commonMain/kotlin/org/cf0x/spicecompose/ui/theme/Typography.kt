package org.cf0x.spicecompose.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun getTypography(expressive: Boolean): Typography {
    val baseline = Typography()
    
    if (!expressive) return baseline

    // M3E Emphasized Typography
    return Typography(
        displayLarge = baseline.displayLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
        displayMedium = baseline.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
        displaySmall = baseline.displaySmall.copy(fontWeight = FontWeight.Bold),
        headlineLarge = baseline.headlineLarge.copy(fontWeight = FontWeight.Bold),
        headlineMedium = baseline.headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = baseline.headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = baseline.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
        titleMedium = baseline.titleMedium.copy(fontWeight = FontWeight.Bold),
        titleSmall = baseline.titleSmall.copy(fontWeight = FontWeight.Bold),
        bodyLarge = baseline.bodyLarge.copy(fontWeight = FontWeight.Medium),
        bodyMedium = baseline.bodyMedium.copy(fontWeight = FontWeight.Medium),
        labelLarge = baseline.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
    )
}
