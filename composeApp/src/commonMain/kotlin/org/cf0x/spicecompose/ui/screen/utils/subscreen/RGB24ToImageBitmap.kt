package org.cf0x.spicecompose.ui.screen.utils.subscreen

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Convert raw RGB24 pixel data to a Compose [ImageBitmap].
 *
 * @param width Image width in pixels
 * @param height Image height in pixels
 * @param rgb24 RGB24 byte array (3 bytes per pixel, row-major, R-G-B order)
 */
expect fun rgb24ToImageBitmap(width: Int, height: Int, rgb24: ByteArray): ImageBitmap?
