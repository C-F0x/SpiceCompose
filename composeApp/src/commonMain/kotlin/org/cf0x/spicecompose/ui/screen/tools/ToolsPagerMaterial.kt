package org.cf0x.spicecompose.ui.screen.tools

import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsPagerMaterial(
    actions: ToolsScreenActions,
) {
    val strings = LocalAppStrings.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.tools) },
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
                FeatureItem(strings.buttons, strings.buttonsSummary, Icons.Rounded.RadioButtonChecked, actions.onOpenButtons)
                FeatureItem(strings.analogs, strings.analogsSummary, Icons.Rounded.LinearScale, actions.onOpenAnalogs)
                FeatureItem(strings.lights, strings.lightsSummary, Icons.Rounded.Lightbulb, actions.onOpenLights)
                HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                FeatureItem(strings.coins, strings.coinsSummary, Icons.Rounded.MonetizationOn, actions.onOpenCoins)
                FeatureItem(strings.keypad, strings.keypadSummary, Icons.Rounded.Apps, actions.onOpenKeypad)
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
