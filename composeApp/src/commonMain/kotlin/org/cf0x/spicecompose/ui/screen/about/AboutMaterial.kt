package org.cf0x.spicecompose.ui.screen.about

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.theme.ThemePreferences

@ExperimentalMaterial3Api
@Composable
fun AboutScreenMaterial(
    uiState: AboutUiState,
    actions: AboutScreenActions,
) {
    val strings       = LocalAppStrings.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState     = rememberLazyListState()
    var logoHeightPx by remember { mutableIntStateOf(0) }
    val fullscreen = LocalFullscreenMode.current
    val p = ThemePreferences

    SpiceBackHandler(enabled = fullscreen.value) {
        fullscreen.value = false
    }

    val scrollProgress by remember {
        derivedStateOf {
            if (logoHeightPx <= 0) 0f
            else {
                val idx    = listState.firstVisibleItemIndex
                val offset = listState.firstVisibleItemScrollOffset
                if (idx > 0) 1f else (offset.toFloat() / logoHeightPx).coerceIn(0f, 1f)
            }
        }
    }
    val titleAlpha by animateFloatAsState(targetValue = scrollProgress, label = "titleAlpha")
    val logoAlpha  by animateFloatAsState(targetValue = 1f - scrollProgress, label = "logoAlpha")

    Scaffold(
        topBar = {
            if (!fullscreen.value && !p.toolbarHidden) {
                TopAppBar(
                    title = { Text(uiState.appName, modifier = Modifier.alpha(titleAlpha)) },
                    navigationIcon = {
                        IconButton(onClick = actions.onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                        }
                    },
                    actions = {
                        FullscreenAction()
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
    ) { innerPadding ->
        val padding = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = padding,
        ) {
            // ── Logo header ───────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { logoHeightPx = it.size.height }
                        .alpha(logoAlpha)
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Code,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = uiState.appName,
                        fontSize = 26.sp,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = uiState.versionName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // ── Links ─────────────────────────────────────────────────────────
            item {
                HorizontalDivider()
                ListItem(
                    modifier = Modifier.clickable { actions.onOpenLink(GITHUB_URL) },
                    headlineContent = { Text(strings.github) },
                    supportingContent = { Text(GITHUB_URL) },
                    leadingContent = { Icon(Icons.Rounded.Code, null) },
                    trailingContent = { Icon(Icons.Rounded.OpenInBrowser, null) },
                )
            }
        }
    }
}
