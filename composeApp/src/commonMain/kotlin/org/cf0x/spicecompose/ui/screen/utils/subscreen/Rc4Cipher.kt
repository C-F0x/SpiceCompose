package org.cf0x.spicecompose.ui.screen.utils.subscreen

/** RC4 stream cipher — compatible with Spice2x's util::RC4. */
class Rc4Cipher(key: ByteArray) {
    private var a = 0
    private var b = 0
    private val sbox = ByteArray(256) { it.toByte() }

    init {
        var j = 0
        for (i in 0..255) {
            j = (j + (sbox[i].toInt() and 0xFF) + (key[i % key.size].toInt() and 0xFF)) % 256
            val tmp = sbox[i]
            sbox[i] = sbox[j]
            sbox[j] = tmp
        }
    }

    /** Encrypt/decrypt in-place (RC4 is symmetric). */
    fun crypt(data: ByteArray) {
        for (i in data.indices) {
            a = (a + 1) % 256
            b = (b + (sbox[a].toInt() and 0xFF)) % 256
            val tmp = sbox[a]
            sbox[a] = sbox[b]
            sbox[b] = tmp
            val k = sbox[((sbox[a].toInt() and 0xFF) + (sbox[b].toInt() and 0xFF)) % 256]
            data[i] = (data[i].toInt() xor (k.toInt() and 0xFF)).toByte()
        }
    }
}
