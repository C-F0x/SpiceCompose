package org.cf0x.spicecompose.ui.component.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.theme.CustomPreferences

@Composable
fun SideRail(
    modifier: Modifier = Modifier,
) {
    val expanded = CustomPreferences.sidebarExpanded
    val p = CustomPreferences
    when (LocalUiMode.current) {
        UiMode.Miuix    -> SideRailMiuix(expanded, { p.updateSidebarExpanded(!expanded) }, modifier)
        UiMode.Material -> SideRailMaterial(expanded, { p.updateSidebarExpanded(!expanded) }, modifier)
    }
}
