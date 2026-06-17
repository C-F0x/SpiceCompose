package org.cf0x.spicecompose.ui.screen.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.component.TonalCard
import org.cf0x.spicecompose.ui.theme.SpiceTheme

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
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 16.dp),
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            
            item {
                TonalCard(shape = SpiceTheme.containerShape(), onClick = actions.onOpenSubScreen) {
                    FeatureItemCapsule(strings.subScreen, strings.subScreenSummary, Icons.Rounded.Tv)
                }
            }

            item {
                TonalCard(shape = SpiceTheme.containerShape(), onClick = actions.onOpenPatches) {
                    FeatureItemCapsule(strings.patches, strings.patchesSummary, Icons.Rounded.Build)
                }
            }
            
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun FeatureItemCapsule(
    title: String,
    summary: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
        leadingContent = { Icon(icon, null) },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    )
}
