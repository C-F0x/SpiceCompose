package org.cf0x.spicecompose.ui.screen.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilsPagerMaterial(
    actions: UtilsScreenActions,
) {
    val strings = LocalAppStrings.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.utils) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = innerPadding,
        ) {
            item {
                FeatureItem(strings.subScreen, strings.subScreenSummary, Icons.Rounded.Tv, actions.onOpenSubScreen)
                HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                FeatureItem(strings.patches, strings.patchesSummary, Icons.Rounded.Build, actions.onOpenPatches)
                FeatureItem(strings.cardManager, strings.cardManagerSummary, Icons.Rounded.Nfc, actions.onOpenCardMgr)
            }
        }
    }
}

@Composable
private fun FeatureItem(
    title: String,
    summary: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
        leadingContent = { Icon(icon, null) },
    )
}
