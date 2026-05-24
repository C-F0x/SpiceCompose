package org.cf0x.spicecompose.ui.screen.about

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun AboutScreenMiuix(
    uiState: AboutUiState,
    actions: AboutScreenActions,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val strings        = LocalAppStrings.current
    val listState      = rememberLazyListState()
    var logoHeightPx by remember { mutableIntStateOf(0) }

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
            SmallTopAppBar(
                title = uiState.appName,
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(MiuixIcons.Back, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        popupHost = {},
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxHeight()
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp),
            contentPadding = innerPadding,
            overscrollEffect = null,
        ) {
            // ── Logo header ───────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { logoHeightPx = it.size.height }
                        .alpha(logoAlpha)
                        .padding(top = 24.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Code,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                    Text(
                        text = uiState.appName,
                        fontSize = 26.sp,
                        color = MiuixTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = uiState.versionName,
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                }
            }

            // ── Links card ────────────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .fillMaxWidth(),
                ) {
                    ArrowPreference(
                        title = strings.github,
                        summary = GITHUB_URL,
                        startAction = {
                            Icon(
                                imageVector = Icons.Rounded.Code,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp),
                                tint = MiuixTheme.colorScheme.onBackground,
                            )
                        },
                        onClick = { actions.onOpenLink(GITHUB_URL) },
                    )
                }
            }
        }
    }
}
