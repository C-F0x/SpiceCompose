package org.cf0x.spicecompose.ui.screen.utils.subscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.cf0x.spicecompose.network.LocalConnectionManager
import org.cf0x.spicecompose.network.TouchControl
import org.cf0x.spicecompose.network.spiceapi.wrappers.captureGetJPG

@Composable
fun SubScreenContent() {
    val connectionManager = LocalConnectionManager.current
    val connection = connectionManager.getConnection()
    val scope = rememberCoroutineScope()
    val touchControl = remember { TouchControl(connectionManager) }
    
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var nativeSize by remember { mutableStateOf(IntSize(0, 0)) }
    var displaySize by remember { mutableStateOf(IntSize(0, 0)) }
    
    val touchPoints = remember { mutableMapOf<Long, Int>() } // PointerID to TouchID

    LaunchedEffect(connection) {
        if (connection == null) return@LaunchedEffect
        while (isActive) {
            try {
                val cap = connection.captureGetJPG(screen = 1, quality = 60, divide = 1)
                if (cap.data.isNotEmpty()) {
                    imageBitmap = decodeToImageBitmap(cap.data)
                    nativeSize = IntSize(cap.width, cap.height)
                }
            } catch (e: Exception) {
                // ignore
            }
            delay(33) // ~30 FPS
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onGloballyPositioned { displaySize = it.size }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val type = event.type
                        
                        event.changes.forEach { change ->
                            val pointerId = change.id.value
                            
                            // Map local to native
                            if (nativeSize.width > 0 && displaySize.width > 0) {
                                // Simple mapping assuming contain fit
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
                                
                                scope.launch {
                                    when (type) {
                                        PointerEventType.Press -> {
                                            val id = touchControl.touchDown(tx, ty)
                                            touchPoints[pointerId] = id
                                        }
                                        PointerEventType.Move -> {
                                            touchPoints[pointerId]?.let { id ->
                                                touchControl.touchMove(id, tx, ty)
                                            }
                                        }
                                        PointerEventType.Release -> {
                                            touchPoints.remove(pointerId)?.let { id ->
                                                touchControl.touchUp(id)
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val img = imageBitmap
        if (img != null) {
            Image(
                bitmap = img,
                contentDescription = "Sub Screen",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Text("Sub Screen not available :(", color = Color.White)
        }
    }
}
