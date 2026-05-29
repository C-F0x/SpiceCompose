package org.cf0x.spicecompose.ui.screen.utils

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import org.cf0x.spicecompose.ui.LocalInSubPage
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.screen.utils.subscreen.SubScreen
import org.cf0x.spicecompose.ui.screen.feature.*

private const val ROUTE_MAIN        = "main"
private const val ROUTE_SUB_SCREEN  = "sub_screen"
private const val ROUTE_PATCHES     = "patches"
private const val ROUTE_CARD_MGR    = "card_mgr"
private const val ROUTE_CABINET     = "cabinet"

@Composable
fun UtilsScreen() {
    var route by rememberSaveable { mutableStateOf(ROUTE_MAIN) }

    val inSubPage = LocalInSubPage.current
    SideEffect { inSubPage.value = route != ROUTE_MAIN }

    SpiceBackHandler(enabled = route != ROUTE_MAIN) { route = ROUTE_MAIN }

    when (route) {
        ROUTE_SUB_SCREEN -> SubScreen(onBack = { route = ROUTE_MAIN })
        ROUTE_PATCHES    -> PatchesScreen(onBack = { route = ROUTE_MAIN })
        ROUTE_CARD_MGR   -> CardManagerScreen(onBack = { route = ROUTE_MAIN })
        ROUTE_CABINET    -> CabinetInfoScreen(onBack = { route = ROUTE_MAIN })
        else -> {
            val actions = UtilsScreenActions(
                onOpenSubScreen = { route = ROUTE_SUB_SCREEN },
                onOpenPatches   = { route = ROUTE_PATCHES },
                onOpenCardMgr   = { route = ROUTE_CARD_MGR },
                onOpenCabinet   = { route = ROUTE_CABINET },
            )
            when (LocalUiMode.current) {
                UiMode.Miuix    -> UtilsPagerMiuix(actions)
                UiMode.Material -> UtilsPagerMaterial(actions)
            }
        }
    }
}

data class UtilsScreenActions(
    val onOpenSubScreen: () -> Unit,
    val onOpenPatches:   () -> Unit,
    val onOpenCardMgr:   () -> Unit,
    val onOpenCabinet:   () -> Unit,
)
