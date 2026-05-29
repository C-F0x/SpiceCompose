package org.cf0x.spicecompose.ui.screen.utils

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import org.cf0x.spicecompose.ui.LocalInSubPage
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.screen.utils.subscreen.SubScreen

private const val ROUTE_MAIN = "main"
private const val ROUTE_SUB_SCREEN = "sub_screen"

@Composable
fun UtilsScreen() {
    var route by rememberSaveable { mutableStateOf(ROUTE_MAIN) }

    // Notify MainScreen to disable pager swipe when in a sub-page
    val inSubPage = LocalInSubPage.current
    SideEffect { inSubPage.value = route != ROUTE_MAIN }

    // Intercept back gesture on sub-pages only
    SpiceBackHandler(enabled = route != ROUTE_MAIN) { route = ROUTE_MAIN }

    when (route) {
        ROUTE_SUB_SCREEN -> SubScreen(onBack = { route = ROUTE_MAIN })
        else -> {
            val onOpenSubScreen = { route = ROUTE_SUB_SCREEN }
            when (LocalUiMode.current) {
                UiMode.Miuix -> UtilsPagerMiuix(onOpenSubScreen)
                UiMode.Material -> UtilsPagerMaterial(onOpenSubScreen)
            }
        }
    }
}
