package org.cf0x.spicecompose.ui.navigation

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainPagerState(
    val pagerState: PagerState,
    private val scope: CoroutineScope,
) {
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    var isNavigating by mutableStateOf(false)
        private set

    private val _resetEvents = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val resetEvents = _resetEvents.asSharedFlow()

    private var navJob: Job? = null
    var lastPage = pagerState.currentPage

    fun emitReset(index: Int) {
        scope.launch { _resetEvents.emit(index) }
    }

    fun animateToPage(target: Int) {
        if (target == selectedPage) {
            // Req 1: Re-tap current tab -> Reset it
            emitReset(target)
            return
        }

        navJob?.cancel()
        selectedPage  = target
        isNavigating  = true
        // ... rest
        // ... (rest same)

        val distance   = abs(target - pagerState.currentPage).coerceAtLeast(2)
        val duration   = 100 * distance + 100
        val info       = pagerState.layoutInfo
        val pageSize   = info.pageSize + info.pageSpacing
        val distPages  = target - pagerState.currentPage - pagerState.currentPageOffsetFraction
        val scrollPx   = distPages * pageSize

        navJob = scope.launch {
            val myJob = coroutineContext.job
            try {
                pagerState.animateScrollBy(
                    value         = scrollPx,
                    animationSpec = tween(easing = EaseInOut, durationMillis = duration),
                )
            } finally {
                if (navJob == myJob) {
                    isNavigating = false
                    if (pagerState.currentPage != target) selectedPage = pagerState.currentPage
                }
            }
        }
    }

    fun syncPage() {
        if (!isNavigating && selectedPage != pagerState.currentPage)
            selectedPage = pagerState.currentPage
    }
}

@Composable
fun rememberMainPagerState(
    pagerState: PagerState,
    scope: CoroutineScope = rememberCoroutineScope(),
): MainPagerState = remember(pagerState, scope) { MainPagerState(pagerState, scope) }

val LocalMainPagerState = staticCompositionLocalOf<MainPagerState> {
    error("No MainPagerState provided")
}
