package org.cf0x.spicecompose.network.spiceapi

class RC4(key: ByteArray) {
    private var a = 0
    private var b = 0
    private val sBox = IntArray(256) { it }

    init {
        var j = 0
        for (i in 0 until 256) {
            j = (j + sBox[i] + (key[i % key.size].toInt() and 0xFF)) % 256
            val tmp = sBox[i]
            sBox[i] = sBox[j]
            sBox[j] = tmp
        }
    }

    fun crypt(data: ByteArray) {
        for (i in data.indices) {
            a = (a + 1) % 256
            b = (b + sBox[a]) % 256

            val tmp = sBox[a]
            sBox[a] = sBox[b]
            sBox[b] = tmp

            val k = sBox[(sBox[a] + sBox[b]) % 256]
            data[i] = (data[i].toInt() xor k).toByte()
        }
    }
}
