package org.cf0x.spicecompose.ui.screen.utils.subscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.TouchControl
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.network.spiceapi.wrappers.captureGetJPG
import org.cf0x.spicecompose.ui.theme.CaptureMode

@Composable
fun SubScreenContent(
    refreshTrigger: Int = 0,
    captureScreen: Int = 1,
    captureQuality: Int = 70,
    captureDivide: Int = 1,
    pollThreads: Int = 2,
    pollIntervalMs: Long = 0,
    captureMode: CaptureMode = CaptureMode.Original,
    capture2xFps: Int = 60,
    capture2xDivide: Int = 1,
    onShareReady: ((ByteArray) -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    val connectionManager = LocalConnectionManager.current
    val connection = connectionManager.getClient()
    val scope = rememberCoroutineScope()
    val touchControl = remember { TouchControl(connectionManager) }

    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var nativeSize by remember { mutableStateOf(IntSize(0, 0)) }
    var displaySize by remember { mutableStateOf(IntSize(0, 0)) }
    var lastCaptureData by remember { mutableStateOf<ByteArray?>(null) }

    val touchPoints = remember { mutableMapOf<Long, Int>() }

    // ── Original mode: concurrent HTTP polling (N threads, best-effort) ──
    if (captureMode == CaptureMode.Original) {
        LaunchedEffect(connection, refreshTrigger, captureScreen, captureQuality, captureDivide, pollThreads, pollIntervalMs) {
            if (connection == null) return@LaunchedEffect
            // Main-thread arbitration: only frames newer than the last shown one win.
            var lastTimestamp = -1L
            repeat(pollThreads.coerceAtLeast(1)) {
                launch(Dispatchers.Default) {
                    while (isActive) {
                        try {
                            val cap = connection.captureGetJPG(
                                screen = captureScreen,
                                quality = captureQuality,
                                divide = captureDivide
                            )
                            if (cap.data.isNotEmpty()) {
                                // JPEG decode runs off the main thread
                                val bmp = decodeToImageBitmap(cap.data)
                                withContext(Dispatchers.Main) {
                                    if (cap.timestamp >= lastTimestamp) {
                                        lastTimestamp = cap.timestamp
                                        imageBitmap = bmp
                                        nativeSize = IntSize(cap.width, cap.height)
                                        lastCaptureData = cap.data
                                    }
                                }
                            }
                        } catch (_: Exception) { }
                        // Optional frame-gap cap: 0 = fastest (no delay)
                        if (pollIntervalMs > 0) delay(pollIntervalMs)
                    }
                }
            }
        }
    }

    // ── Capture2x mode: WebSocket stream (temporarily disabled — UI only exposes Original) ──
    // if (captureMode == CaptureMode.Capture2x) {
    //     LaunchedEffect(server, capture2xFps, capture2xDivide, captureScreen) {
    //         try {
    //             if (server == null) {
    //                 c2xError = "No server connected"
    //                 return@LaunchedEffect
    //             }
    //             val srv = server!!
    //             c2xError = null
    //             println("[SubScreen] Capture2x: connecting to ${srv.host}:${srv.port}")
    //
    //             // Disconnect previous session if re-launching
    //             c2xConnection?.disconnect()
    //             c2xConnection = null
    //
    //             val conn = Capture2xConnection(srv.host, srv.port, password = srv.password)
    //             c2xConnection = conn
    //             println("[SubScreen] Capture2x: calling connect()...")
    //             val ok = conn.connect(screen = captureScreen, divide = capture2xDivide, fps = capture2xFps)
    //             println("[SubScreen] Capture2x: connect() returned $ok")
    //             if (!ok) {
    //                 c2xError = "Failed to connect capture2x WebSocket"
    //                 return@LaunchedEffect
    //             }
    //
    //             println("[SubScreen] Capture2x: starting frame collection...")
    //             // Collect frames from the flow
    //             conn.frames.collect { rgbImage ->
    //                 val bitmap = rgb24ToImageBitmap(rgbImage.width, rgbImage.height, rgbImage.pixels)
    //                 if (bitmap != null) {
    //                     imageBitmap = bitmap
    //                     nativeSize = IntSize(rgbImage.width, rgbImage.height)
    //                 }
    //             }
    //         } catch (e: Exception) {
    //             println("[SubScreen] Capture2x LaunchedEffect exception: ${e::class.simpleName} → ${e.message}")
    //             c2xError = "Error: ${e.message}"
    //         }
    //     }
    //
    //     // Collect errors
    //     LaunchedEffect(c2xConnection) {
    //         c2xConnection?.error?.collect { msg ->
    //             c2xError = msg
    //         }
    //     }
    // }
    //
    // // Cleanup when leaving composition OR switching modes
    // DisposableEffect(captureMode) {
    //     onDispose {
    //         val conn = c2xConnection
    //         if (conn != null) {
    //             scope.launch { conn.disconnect() }
    //         }
    //     }
    // }

    LaunchedEffect(lastCaptureData) {
        lastCaptureData?.let { onShareReady?.invoke(it) }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val img = imageBitmap
        if (img != null) {
            Image(
                bitmap = img,
                contentDescription = strings.subScreen,
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { displaySize = it.size }
                    .pointerInput(nativeSize) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()

                                event.changes.forEach { change ->
                                    val pointerId = change.id.value

                                    if (nativeSize.width > 0 && displaySize.width > 0) {
                                        val imageAspect = nativeSize.width.toFloat() / nativeSize.height
                                        val displayAspect = displaySize.width.toFloat() / displaySize.height

                                        var actualW = displaySize.width.toFloat()
                                        var actualH = displaySize.height.toFloat()
                                        var padX = 0f
                                        var padY = 0f

                                        if (imageAspect > displayAspect) {
                                            actualH = displaySize.width / imageAspect
                                            padY = (displaySize.height - actualH) / 2
                                        } else {
                                            actualW = displaySize.height * imageAspect
                                            padX = (displaySize.width - actualW) / 2
                                        }

                                        val scaleX = nativeSize.width / actualW
                                        val scaleY = nativeSize.height / actualH

                                        val localX = change.position.x - padX
                                        val localY = change.position.y - padY

                                        val tx = (localX * scaleX).toInt()
                                        val ty = (localY * scaleY).toInt()

                                        // Use per-change pressed state, NOT event.type,
                                        // to correctly handle multi-touch scenarios where
                                        // one change is a Press and another is a Move.
                                        val pressed = change.pressed
                                        val wasPressed = change.previousPressed

                                        scope.launch {
                                            when {
                                                !wasPressed && pressed -> {
                                                    val id = touchControl.touchDown(tx, ty)
                                                    touchPoints[pointerId] = id
                                                }
                                                wasPressed && pressed -> {
                                                    touchPoints[pointerId]?.let { id ->
                                                        touchControl.touchMove(id, tx, ty)
                                                    }
                                                }
                                                wasPressed && !pressed -> {
                                                    touchPoints.remove(pointerId)?.let { id ->
                                                        touchControl.touchUp(id)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                contentScale = ContentScale.Fit
            )
        } else {
            Text(strings.subScreenNotAvailable, color = Color.White)
        }
    }
}
