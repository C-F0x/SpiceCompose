package org.cf0x.spicecompose.ui.screen.utils.subscreen

/**
 * Pure Kotlin QOI (Quite OK Image) decoder.
 *
 * Decodes QOI-encoded RGB/RGBA data into a byte array of RGB24 pixels
 * (3 bytes per pixel, BGR or RGB order depending on usage).
 *
 * Spec: https://qoiformat.org/
 */
object QoiDecoder {

    private const val QOI_MAGIC = 0x716F6966 // "qoif"
    private const val QOI_OP_INDEX = 0x00 // 00xxxxxx
    private const val QOI_OP_DIFF = 0x40   // 01xxxxxx
    private const val QOI_OP_LUMA = 0x80   // 10xxxxxx
    private const val QOI_OP_RUN = 0xC0    // 11xxxxxx
    private const val QOI_OP_RGB = 0xFE    // 11111110
    private const val QOI_OP_RGBA = 0xFF   // 11111111
    private const val QOI_MASK_2 = 0xC0    // 11000000

    private const val QOI_PADDING = 8

    data class QoiHeader(
        val width: Int,
        val height: Int,
        val channels: Int,
        val colorspace: Int
    )

    /**
     * Decode QOI data to RGB24 byte array.
     *
     * @param data QOI encoded bytes (with 14-byte header + pixel chunks + 8-byte padding)
     * @return ByteArray of RGB24 pixels (width × height × 3 bytes), or null on failure
     */
    fun decodeToRgb24(data: ByteArray): Rgb24Image? {
        if (data.size < 22) return null // 14 header + 8 padding minimum

        val header = readHeader(data) ?: return null
        val pixelCount = header.width * header.height
        val expectedSize = pixelCount * 3

        val result = ByteArray(expectedSize)
        val index = Array(64) { Pixel(0, 0, 0, 255) }
        var px = Pixel(0, 0, 0, 255)

        var pos = 14 // skip header
        var outPos = 0

        // End marker: 7 zeros + 1 one
        val endPos = data.size - QOI_PADDING

        while (pos < endPos && outPos < expectedSize) {
            val byte1 = data[pos++].toInt() and 0xFF

            when {
                byte1 == QOI_OP_RGB -> {
                    if (pos + 3 > endPos) break
                    px = Pixel(
                        data[pos].toInt() and 0xFF,
                        data[pos + 1].toInt() and 0xFF,
                        data[pos + 2].toInt() and 0xFF,
                        255
                    )
                    pos += 3
                    writeRgb24Pixel(result, outPos, px)
                    outPos += 3
                    index[px.index()] = px
                }

                byte1 == QOI_OP_RGBA -> {
                    if (pos + 4 > endPos) break
                    px = Pixel(
                        data[pos].toInt() and 0xFF,
                        data[pos + 1].toInt() and 0xFF,
                        data[pos + 2].toInt() and 0xFF,
                        data[pos + 3].toInt() and 0xFF
                    )
                    pos += 4
                    writeRgb24Pixel(result, outPos, px)
                    outPos += 3
                    index[px.index()] = px
                }

                byte1 and QOI_MASK_2 == QOI_OP_INDEX -> {
                    val idx = byte1 and 0x3F
                    px = index[idx]
                    writeRgb24Pixel(result, outPos, px)
                    outPos += 3
                }

                byte1 and QOI_MASK_2 == QOI_OP_DIFF -> {
                    val dr = ((byte1 shr 4) and 0x03) - 2
                    val dg = ((byte1 shr 2) and 0x03) - 2
                    val db = (byte1 and 0x03) - 2
                    px = Pixel(
                        (px.r + dr).coerceIn(0, 255),
                        (px.g + dg).coerceIn(0, 255),
                        (px.b + db).coerceIn(0, 255),
                        px.a
                    )
                    writeRgb24Pixel(result, outPos, px)
                    outPos += 3
                    index[px.index()] = px
                }

                byte1 and QOI_MASK_2 == QOI_OP_LUMA -> {
                    if (pos >= endPos) break
                    val byte2 = data[pos++].toInt() and 0xFF
                    val dg = (byte1 and 0x3F) - 32
                    val dr_dg = (byte2 shr 4) and 0x0F
                    val db_dg = byte2 and 0x0F
                    val dr = dr_dg - 8 + dg
                    val db = db_dg - 8 + dg
                    px = Pixel(
                        (px.r + dr).coerceIn(0, 255),
                        (px.g + dg).coerceIn(0, 255),
                        (px.b + db).coerceIn(0, 255),
                        px.a
                    )
                    writeRgb24Pixel(result, outPos, px)
                    outPos += 3
                    index[px.index()] = px
                }

                byte1 and QOI_MASK_2 == QOI_OP_RUN -> {
                    val run = (byte1 and 0x3F) + 1
                    for (i in 0 until run) {
                        if (outPos + 3 > expectedSize) break
                        writeRgb24Pixel(result, outPos, px)
                        outPos += 3
                    }
                    index[px.index()] = px
                }

                else -> {
                    // Unexpected byte, stop decoding
                    break
                }
            }
        }

        return Rgb24Image(header.width, header.height, result)
    }

    private fun readHeader(data: ByteArray): QoiHeader? {
        if (data.size < 14) return null
        val magic = (data[0].toInt() shl 24) or
                (data[1].toInt() shl 16) or
                (data[2].toInt() shl 8) or
                (data[3].toInt() and 0xFF)
        if (magic != QOI_MAGIC) return null

        val width = ((data[4].toInt() and 0xFF) shl 24) or
                ((data[5].toInt() and 0xFF) shl 16) or
                ((data[6].toInt() and 0xFF) shl 8) or
                (data[7].toInt() and 0xFF)
        val height = ((data[8].toInt() and 0xFF) shl 24) or
                ((data[9].toInt() and 0xFF) shl 16) or
                ((data[10].toInt() and 0xFF) shl 8) or
                (data[11].toInt() and 0xFF)
        val channels = data[12].toInt() and 0xFF
        val colorspace = data[13].toInt() and 0xFF

        if (width <= 0 || height <= 0 || width > 16384 || height > 16384) return null
        if (channels != 3 && channels != 4) return null

        return QoiHeader(width, height, channels, colorspace)
    }

    private fun writeRgb24Pixel(dest: ByteArray, offset: Int, px: Pixel) {
        if (offset + 3 > dest.size) return
        dest[offset] = px.r.toByte()
        dest[offset + 1] = px.g.toByte()
        dest[offset + 2] = px.b.toByte()
    }

    private data class Pixel(val r: Int, val g: Int, val b: Int, val a: Int) {
        fun index(): Int = (r * 3 + g * 5 + b * 7 + a * 11) % 64
    }
}

data class Rgb24Image(
    val width: Int,
    val height: Int,
    /** RGB24 pixel data — 3 bytes per pixel, row-major */
    val pixels: ByteArray
)
