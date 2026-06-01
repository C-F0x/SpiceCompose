package org.cf0x.spicecompose.util

@OptIn(ExperimentalUnsignedTypes::class)
object CardCipher {
    private const val CIPHER_CHARS = "0123456789ABCDEFGHJKLMNPRSTUWXYZ"

    private fun decodeHex(hex: String): ByteArray {
        val result = ByteArray(hex.length / 2)
        for (i in result.indices) {
            result[i] = hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return result
    }

    private fun encodeHex(bytes: ByteArray): String {
        return bytes.joinToString("") { it.toUByte().toString(16).uppercase().padStart(2, '0') }
    }

    fun encode(cardID: String): String {
        if (cardID.length != 16) throw Exception("cardID must be of length 16")
        val capitalized = cardID.uppercase()
        val cardIDData = decodeHex(capitalized).reversedArray()

        val cipher = cardIDData.map { it.toUByte().toInt() }.toMutableList()
        while (cipher.size < 8) cipher.add(0)
        
        unpack(cipher, cipher2(0x00, pack(cipher)))
        unpack(cipher, cipher1(0x20, pack(cipher)))
        unpack(cipher, cipher2(0x40, pack(cipher)))

        val bits = IntArray(65)
        for (i in 0 until 64) {
            bits[i] = (cipher[i shr 3] shr (i.inv() and 7)) and 1
        }
        bits[64] = 0

        val parts = IntArray(16)
        for (i in 0 until 13) {
            parts[i] = 0
            for (n in 0 until 5) {
                parts[i] = parts[i] or (bits[i * 5 + n] shl (4 - n))
            }
        }

        val cardType = getCardType(capitalized)
        parts[0] = parts[0] xor cardType
        parts[13] = 1
        for (i in 1 until 14) {
            parts[i] = parts[i] xor parts[i - 1]
        }

        parts[14] = cardType
        parts[15] = calculateChecksum(parts)

        return parts.joinToString("") { CIPHER_CHARS[it].toString() }
    }

    fun decode(cipherText: String): String {
        val ct = cipherText.uppercase().trim().replace("-", "").replace(" ", "").replace("O", "0").replace("I", "1")
        if (ct.length != 16) throw Exception("Cipher not of length 16: $ct")

        val parts = IntArray(16)
        for (i in 0 until 16) {
            val v = CIPHER_CHARS.indexOf(ct[i])
            if (v < 0) throw Exception("Invalid card cipher character: ${ct[i]}")
            parts[i] = v
        }

        for (i in 13 downTo 1) {
            parts[i] = parts[i] xor parts[i - 1]
        }
        parts[0] = parts[0] xor parts[14]

        val bits = IntArray(64)
        for (i in 0 until 64) {
            bits[i] = (parts[i / 5] shr (4 - (i % 5))) and 1
        }

        val cipherBytes = IntArray(8)
        for (i in 0 until 64) {
            cipherBytes[i / 8] = cipherBytes[i / 8] or (bits[i] shl (i.inv() and 7))
        }

        val decipheredBytes = mutableListOf<Int>()
        repeat(8) { decipheredBytes.add(0) }
        
        unpack(decipheredBytes, cipher1(0x40, pack(cipherBytes.toList())))
        unpack(decipheredBytes, cipher2(0x20, pack(decipheredBytes)))
        unpack(decipheredBytes, cipher1(0x00, pack(decipheredBytes)))

        return encodeHex(decipheredBytes.map { it.toByte() }.toByteArray().reversedArray())
    }

    private fun cipher1(off: Int, state: Long): Long {
        var higher = (state shr 32).toInt()
        var lower = (state and 0xFFFFFFFFL).toInt()

        for (i in 0 until 32 step 4) {
            val lowerROR = (higher xor KEY[off + 31 - i]).rotateRight(28)
            var lowerXOR = 0
            lowerXOR = lowerXOR xor LOOKUP1[((higher xor KEY[off + 30 - i]) shr 26) and 0x3F]
            lowerXOR = lowerXOR xor LOOKUP2[((higher xor KEY[off + 30 - i]) shr 18) and 0x3F]
            lowerXOR = lowerXOR xor LOOKUP3[((higher xor KEY[off + 30 - i]) shr 10) and 0x3F]
            lowerXOR = lowerXOR xor LOOKUP4[((higher xor KEY[off + 30 - i]) shr 2) and 0x3F]
            lowerXOR = lowerXOR xor LOOKUP5[(lowerROR shr 26) and 0x3F]
            lowerXOR = lowerXOR xor LOOKUP6[(lowerROR shr 18) and 0x3F]
            lowerXOR = lowerXOR xor LOOKUP7[(lowerROR shr 10) and 0x3F]
            lowerXOR = lowerXOR xor LOOKUP8[(lowerROR shr 2) and 0x3F]
            lower = lower xor lowerXOR

            val higherROR = (lower xor KEY[off + 29 - i]).rotateRight(28)
            var higherXOR = 0
            higherXOR = higherXOR xor LOOKUP1[((lower xor KEY[off + 28 - i]) shr 26) and 0x3F]
            higherXOR = higherXOR xor LOOKUP2[((lower xor KEY[off + 28 - i]) shr 18) and 0x3F]
            higherXOR = higherXOR xor LOOKUP3[((lower xor KEY[off + 28 - i]) shr 10) and 0x3F]
            higherXOR = higherXOR xor LOOKUP4[((lower xor KEY[off + 28 - i]) shr 2) and 0x3F]
            higherXOR = higherXOR xor LOOKUP5[(higherROR shr 26) and 0x3F]
            higherXOR = higherXOR xor LOOKUP6[(higherROR shr 18) and 0x3F]
            higherXOR = higherXOR xor LOOKUP7[(higherROR shr 10) and 0x3F]
            higherXOR = higherXOR xor LOOKUP8[(higherROR shr 2) and 0x3F]
            higher = higher xor higherXOR
        }

        return (higher.toLong() shl 32) or (lower.toLong() and 0xFFFFFFFFL)
    }

    private fun cipher2(off: Int, state: Long): Long {
        var higher = (state shr 32).toInt()
        var lower = (state and 0xFFFFFFFFL).toInt()

        for (i in 0 until 32 step 4) {
            val lowerROR = (higher xor KEY[off + i + 1]).rotateRight(28)
            var lowerXOR = 0
            lowerXOR = lowerXOR xor LOOKUP5[(lowerROR shr 26) and 0x3F]
            lowerXOR = lowerXOR xor LOOKUP6[(lowerROR shr 18) and 0x3F]
            lowerXOR = lowerXOR xor LOOKUP7[(lowerROR shr 10) and 0x3F]
            lowerXOR = lowerXOR xor LOOKUP8[(lowerROR shr 2) and 0x3F]
            lowerXOR = lowerXOR xor LOOKUP1[((higher xor KEY[off + i]) shr 26) and 0x3F]
            lowerXOR = lowerXOR xor LOOKUP2[((higher xor KEY[off + i]) shr 18) and 0x3F]
            lowerXOR = lowerXOR xor LOOKUP3[((higher xor KEY[off + i]) shr 10) and 0x3F]
            lowerXOR = lowerXOR xor LOOKUP4[((higher xor KEY[off + i]) shr 2) and 0x3F]
            lower = lower xor lowerXOR

            val higherROR = (lower xor KEY[off + i + 3]).rotateRight(28)
            var higherXOR = 0
            higherXOR = higherXOR xor LOOKUP5[(higherROR shr 26) and 0x3F]
            higherXOR = higherXOR xor LOOKUP6[(higherROR shr 18) and 0x3F]
            higherXOR = higherXOR xor LOOKUP7[(higherROR shr 10) and 0x3F]
            higherXOR = higherXOR xor LOOKUP8[(higherROR shr 2) and 0x3F]
            higherXOR = higherXOR xor LOOKUP1[((lower xor KEY[off + i + 2]) shr 26) and 0x3F]
            higherXOR = higherXOR xor LOOKUP2[((lower xor KEY[off + i + 2]) shr 18) and 0x3F]
            higherXOR = higherXOR xor LOOKUP3[((lower xor KEY[off + i + 2]) shr 10) and 0x3F]
            higherXOR = higherXOR xor LOOKUP4[((lower xor KEY[off + i + 2]) shr 2) and 0x3F]
            higher = higher xor higherXOR
        }

        return (higher.toLong() shl 32) or (lower.toLong() and 0xFFFFFFFFL)
    }

    private fun getCardType(cardID: String): Int {
        return when {
            cardID.startsWith("E0") -> 1
            cardID.startsWith("01") -> 2
            else -> throw Exception("Unknown card type: $cardID")
        }
    }

    private fun calculateChecksum(buffer: IntArray): Int {
        var chk = 0
        for (i in buffer.indices) {
            chk += (i % 3 + 1) * (buffer[i] and 0xFF)
        }
        while (chk >= 0x20) {
            chk = (chk and 0x1F) + (chk shr 5)
        }
        return chk and 0xFF
    }

    private fun Int.rotateRight(amount: Int): Int {
        return (this ushr amount) or (this shl (32 - amount))
    }

    private fun unpack(buffer: MutableList<Int>, state: Long) {
        val i1 = (state shr 32).toInt()
        val i2 = state.toInt()
        val i3 = i2.rotateRight(31)
        val i4 = (i1 xor i3) and 0x55555555
        val i5 = i4 xor i3
        val i6 = (i4 xor i1).rotateRight(31)
        val i7 = (i6 xor (i5 ushr 8)) and 0x00FF00FF
        val i8 = i5 xor (i7 shl 8)
        val i9 = i7 xor i6
        val i10 = ((i9 ushr 2) xor i8) and 0x33333333
        val i11 = (i10 shl 2) xor i9
        val i12 = i10 xor i8
        val i13 = (i11 xor (i12 ushr 16)) and 0x0000FFFF
        val i14 = i12 xor (i13 shl 16)
        val i15 = i13 xor i11
        val i16 = (i14 xor (i15 ushr 4)) and 0x0F0F0F0F
        val i17 = (i16 shl 4) xor i15
        val i18 = i16 xor i14

        buffer[0] = i18 and 0xFF
        buffer[1] = (i18 ushr 8) and 0xFF
        buffer[2] = (i18 ushr 16) and 0xFF
        buffer[3] = (i18 ushr 24) and 0xFF
        buffer[4] = i17 and 0xFF
        buffer[5] = (i17 ushr 8) and 0xFF
        buffer[6] = (i17 ushr 16) and 0xFF
        buffer[7] = (i17 ushr 24) and 0xFF
    }

    private fun pack(buffer: List<Int>): Long {
        val val1 = (buffer[0] and 0xFF) or ((buffer[1] and 0xFF) shl 8) or ((buffer[2] and 0xFF) shl 16) or ((buffer[3] and 0xFF) shl 24)
        val val2 = (buffer[4] and 0xFF) or ((buffer[5] and 0xFF) shl 8) or ((buffer[6] and 0xFF) shl 16) or ((buffer[7] and 0xFF) shl 24)

        val i1 = (((val1 xor (val2 ushr 4)) and 0x0F0F0F0F) shl 4) xor val2
        val i2 = ((val1 xor (val2 ushr 4)) and 0x0F0F0F0F) xor val1
        val i3 = (i1 xor (i2 ushr 16)) and 0x0000FFFF
        val i4 = ((i1 xor (i2 ushr 16)) shl 16) xor i2
        val i5 = i3 xor i1
        val i6 = (i4 xor (i5 ushr 2)) and 0x33333333
        val i7 = i5 xor (i6 shl 2)
        val i8 = i6 xor i4
        val i9 = (i7 xor (i8 ushr 8)) and 0x00FF00FF
        val i10 = i8 xor (i9 shl 8)
        val i11 = (i9 xor i7).rotateRight(1)
        val i12 = (i10 xor i11) and 0x55555555
        val i13 = (i12 xor i10).rotateRight(1)
        val i14 = i12 xor i11

        return (i13.toLong() shl 32) or (i14.toLong() and 0xFFFFFFFFL)
    }

    private val KEY = intArrayOf(
        0x20d0d03cu.toInt(), 0x868ecb41u.toInt(), 0xbcd89c84u.toInt(), 0x4c0e0d0du.toInt(),
        0x84fc30acu.toInt(), 0x4cc1890eu.toInt(), 0xfc5418a4u.toInt(), 0x02c50f44u.toInt(),
        0x68acb4e0u.toInt(), 0x06cd4a4eu.toInt(), 0xcc28906cu.toInt(), 0x4f0c8ac0u.toInt(),
        0xb03ca468u.toInt(), 0x884ac7c4u.toInt(), 0x389490d8u.toInt(), 0xcf80c6c2u.toInt(),
        0x58d87404u.toInt(), 0xc48ec444u.toInt(), 0xb4e83c50u.toInt(), 0x498d0147u.toInt(),
        0x64f454c0u.toInt(), 0x4c4701c8u.toInt(), 0xec302cc4u.toInt(), 0xc6c949c1u.toInt(),
        0xc84c00f0u.toInt(), 0xcdcc49ccu.toInt(), 0x883c5cf4u.toInt(), 0x8b0fcb80u.toInt(),
        0x703cc0b0u.toInt(), 0xcb820a8du.toInt(), 0x78804c8cu.toInt(), 0x4fca830eu.toInt(),
        0x80d0f03cu.toInt(), 0x8ec84f8cu.toInt(), 0x98c89c4cu.toInt(), 0xc80d878fu.toInt(),
        0x54bc949cu.toInt(), 0xc801c5ceu.toInt(), 0x749078dcu.toInt(), 0xc3c80d46u.toInt(),
        0x2c8070f0u.toInt(), 0x0cce4dcfu.toInt(), 0x8c3874e4u.toInt(), 0x8d448ac3u.toInt(),
        0x987cac70u.toInt(), 0xc0c20ac5u.toInt(), 0x288cfc78u.toInt(), 0xc28543c8u.toInt(),
        0x4c8c7434u.toInt(), 0xc50e4f8du.toInt(), 0x8468f4b4u.toInt(), 0xcb4a0307u.toInt(),
        0x2854dc98u.toInt(), 0x48430b45u.toInt(), 0x6858fce8u.toInt(), 0x4681cd49u.toInt(),
        0xd04808ecu.toInt(), 0x458d0fcbu.toInt(), 0xe0a48ce4u.toInt(), 0x880f8fceu.toInt(),
        0x7434b8fcu.toInt(), 0xce080a8eu.toInt(), 0x5860fc6cu.toInt(), 0x46c886ccu.toInt(),
        0xd01098a4u.toInt(), 0xce090b8cu.toInt(), 0x1044cc2cu.toInt(), 0x86898e0fu.toInt(),
        0xd0809c3cu.toInt(), 0x4a05860fu.toInt(), 0x54b4f80cu.toInt(), 0x4008870eu.toInt(),
        0x1480b88cu.toInt(), 0x0ac8854fu.toInt(), 0x1c9034ccu.toInt(), 0x08444c4eu.toInt(),
        0x0cb83c64u.toInt(), 0x41c08cc6u.toInt(), 0x1c083460u.toInt(), 0xc0c603ceu.toInt(),
        0x2ca0645cu.toInt(), 0x818246cbu.toInt(), 0x0408e454u.toInt(), 0xc5464487u.toInt(),
        0x88607c18u.toInt(), 0xc1424187u.toInt(), 0x284c7c90u.toInt(), 0xc1030509u.toInt(),
        0x40486c94u.toInt(), 0x4603494bu.toInt(), 0xe0404ce4u.toInt(), 0x4109094du.toInt(),
        0x60443ce4u.toInt(), 0x4c0b8b8du.toInt(), 0xe054e8bcu.toInt(), 0x02008e89u.toInt()
    )

    private val LOOKUP1 = intArrayOf(
        0x02080008u.toInt(), 0x02082000u.toInt(), 0x00002008u.toInt(), 0x00000000u.toInt(),
        0x02002000u.toInt(), 0x00080008u.toInt(), 0x02080000u.toInt(), 0x02082008u.toInt(),
        0x00000008u.toInt(), 0x02000000u.toInt(), 0x00082000u.toInt(), 0x00002008u.toInt(),
        0x00082008u.toInt(), 0x02002008u.toInt(), 0x02000008u.toInt(), 0x02080000u.toInt(),
        0x00002000u.toInt(), 0x00082008u.toInt(), 0x00080008u.toInt(), 0x02002000u.toInt(),
        0x02082008u.toInt(), 0x02000008u.toInt(), 0x00000000u.toInt(), 0x00082000u.toInt(),
        0x02000000u.toInt(), 0x00080000u.toInt(), 0x02002008u.toInt(), 0x02080008u.toInt(),
        0x00080000u.toInt(), 0x00002000u.toInt(), 0x02082000u.toInt(), 0x00000008u.toInt(),
        0x00080000u.toInt(), 0x00002000u.toInt(), 0x02000008u.toInt(), 0x02082008u.toInt(),
        0x00002008u.toInt(), 0x02000000u.toInt(), 0x00000000u.toInt(), 0x00082000u.toInt(),
        0x02080008u.toInt(), 0x02002008u.toInt(), 0x02002000u.toInt(), 0x00080008u.toInt(),
        0x02082000u.toInt(), 0x00000008u.toInt(), 0x00080008u.toInt(), 0x02002000u.toInt(),
        0x02082008u.toInt(), 0x00080000u.toInt(), 0x02080000u.toInt(), 0x02000008u.toInt(),
        0x00082000u.toInt(), 0x00002008u.toInt(), 0x02002008u.toInt(), 0x02080000u.toInt(),
        0x00000008u.toInt(), 0x02082000u.toInt(), 0x00082008u.toInt(), 0x00000000u.toInt(),
        0x02000000u.toInt(), 0x02080008u.toInt(), 0x00002000u.toInt(), 0x00082008u.toInt()
    )

    private val LOOKUP2 = intArrayOf(
        0x08000004u.toInt(), 0x00020004u.toInt(), 0x00000000u.toInt(), 0x08020200u.toInt(),
        0x00020004u.toInt(), 0x00000200u.toInt(), 0x08000204u.toInt(), 0x00020000u.toInt(),
        0x00000204u.toInt(), 0x08020204u.toInt(), 0x00020200u.toInt(), 0x08000000u.toInt(),
        0x08000200u.toInt(), 0x08000004u.toInt(), 0x08020000u.toInt(), 0x00020204u.toInt(),
        0x00020000u.toInt(), 0x08000204u.toInt(), 0x08020004u.toInt(), 0x00000000u.toInt(),
        0x00000200u.toInt(), 0x00000004u.toInt(), 0x08020200u.toInt(), 0x08020004u.toInt(),
        0x08020204u.toInt(), 0x08020000u.toInt(), 0x08000000u.toInt(), 0x00000204u.toInt(),
        0x00000004u.toInt(), 0x00020200u.toInt(), 0x00020204u.toInt(), 0x08000200u.toInt(),
        0x00000204u.toInt(), 0x08000000u.toInt(), 0x08000200u.toInt(), 0x00020204u.toInt(),
        0x08020200u.toInt(), 0x00020004u.toInt(), 0x00000000u.toInt(), 0x08000200u.toInt(),
        0x08000000u.toInt(), 0x00000200u.toInt(), 0x08020004u.toInt(), 0x00020000u.toInt(),
        0x00020004u.toInt(), 0x08020204u.toInt(), 0x00020200u.toInt(), 0x00000004u.toInt(),
        0x08020204u.toInt(), 0x00020200u.toInt(), 0x00020000u.toInt(), 0x08000204u.toInt(),
        0x08000004u.toInt(), 0x08020000u.toInt(), 0x00020204u.toInt(), 0x00000000u.toInt(),
        0x00000200u.toInt(), 0x08000004u.toInt(), 0x08000204u.toInt(), 0x08020200u.toInt(),
        0x08020000u.toInt(), 0x00000204u.toInt(), 0x00000004u.toInt(), 0x08020004u.toInt()
    )

    private val LOOKUP3 = intArrayOf(
        0x80040100u.toInt(), 0x01000100u.toInt(), 0x80000000u.toInt(), 0x81040100u.toInt(),
        0x00000000u.toInt(), 0x01040000u.toInt(), 0x81000100u.toInt(), 0x80040000u.toInt(),
        0x01040100u.toInt(), 0x81000000u.toInt(), 0x01000000u.toInt(), 0x80000100u.toInt(),
        0x81000000u.toInt(), 0x80040100u.toInt(), 0x00040000u.toInt(), 0x01000000u.toInt(),
        0x81040000u.toInt(), 0x00040100u.toInt(), 0x00000100u.toInt(), 0x80000000u.toInt(),
        0x00040100u.toInt(), 0x81000100u.toInt(), 0x01040000u.toInt(), 0x00000100u.toInt(),
        0x80000100u.toInt(), 0x00000000u.toInt(), 0x80040000u.toInt(), 0x01040100u.toInt(),
        0x01000100u.toInt(), 0x81040000u.toInt(), 0x81040100u.toInt(), 0x00040000u.toInt(),
        0x81040000u.toInt(), 0x80000100u.toInt(), 0x00040000u.toInt(), 0x81000000u.toInt(),
        0x00040100u.toInt(), 0x01000100u.toInt(), 0x80000000u.toInt(), 0x01040000u.toInt(),
        0x81000100u.toInt(), 0x00000000u.toInt(), 0x00000100u.toInt(), 0x80040000u.toInt(),
        0x00000000u.toInt(), 0x81040000u.toInt(), 0x01040100u.toInt(), 0x00000100u.toInt(),
        0x01000000u.toInt(), 0x81040100u.toInt(), 0x80040100u.toInt(), 0x00040000u.toInt(),
        0x81040100u.toInt(), 0x80000000u.toInt(), 0x01000100u.toInt(), 0x80040100u.toInt(),
        0x80040000u.toInt(), 0x00040100u.toInt(), 0x01040000u.toInt(), 0x81000100u.toInt(),
        0x80000100u.toInt(), 0x01000000u.toInt(), 0x81000000u.toInt(), 0x01040100u.toInt()
    )

    private val LOOKUP4 = intArrayOf(
        0x04010801u.toInt(), 0x00000000u.toInt(), 0x00010800u.toInt(), 0x04010000u.toInt(),
        0x04000001u.toInt(), 0x00000801u.toInt(), 0x04000800u.toInt(), 0x00010800u.toInt(),
        0x00000800u.toInt(), 0x04010001u.toInt(), 0x00000001u.toInt(), 0x04000800u.toInt(),
        0x00010001u.toInt(), 0x04010800u.toInt(), 0x04010000u.toInt(), 0x00000001u.toInt(),
        0x00010000u.toInt(), 0x04000801u.toInt(), 0x04010001u.toInt(), 0x00000800u.toInt(),
        0x00010801u.toInt(), 0x04000000u.toInt(), 0x00000000u.toInt(), 0x00010001u.toInt(),
        0x04000801u.toInt(), 0x00010801u.toInt(), 0x04010800u.toInt(), 0x04000001u.toInt(),
        0x04000000u.toInt(), 0x00010000u.toInt(), 0x00000801u.toInt(), 0x04010801u.toInt(),
        0x00010001u.toInt(), 0x04010800u.toInt(), 0x04000800u.toInt(), 0x00010801u.toInt(),
        0x04010801u.toInt(), 0x00010001u.toInt(), 0x04000001u.toInt(), 0x00000000u.toInt(),
        0x04000000u.toInt(), 0x00000801u.toInt(), 0x00010000u.toInt(), 0x04010001u.toInt(),
        0x00000800u.toInt(), 0x04000000u.toInt(), 0x00010801u.toInt(), 0x04000801u.toInt(),
        0x04010800u.toInt(), 0x00000800u.toInt(), 0x00000000u.toInt(), 0x04000001u.toInt(),
        0x00000001u.toInt(), 0x04010801u.toInt(), 0x00010800u.toInt(), 0x04010000u.toInt(),
        0x04010001u.toInt(), 0x00010000u.toInt(), 0x00000801u.toInt(), 0x04000800u.toInt(),
        0x04000801u.toInt(), 0x00000001u.toInt(), 0x04010000u.toInt(), 0x00010800u.toInt()
    )

    private val LOOKUP5 = intArrayOf(
        0x00000400u.toInt(), 0x00000020u.toInt(), 0x00100020u.toInt(), 0x40100000u.toInt(),
        0x40100420u.toInt(), 0x40000400u.toInt(), 0x00000420u.toInt(), 0x00000000u.toInt(),
        0x00100000u.toInt(), 0x40100020u.toInt(), 0x40000020u.toInt(), 0x00100400u.toInt(),
        0x40000000u.toInt(), 0x00100420u.toInt(), 0x00100400u.toInt(), 0x40000020u.toInt(),
        0x40100020u.toInt(), 0x00000400u.toInt(), 0x40000400u.toInt(), 0x40100420u.toInt(),
        0x00000000u.toInt(), 0x00100020u.toInt(), 0x40100000u.toInt(), 0x00000420u.toInt(),
        0x40100400u.toInt(), 0x40000420u.toInt(), 0x00100420u.toInt(), 0x40000000u.toInt(),
        0x40000420u.toInt(), 0x40100400u.toInt(), 0x00000020u.toInt(), 0x00100000u.toInt(),
        0x40000420u.toInt(), 0x00100400u.toInt(), 0x40100400u.toInt(), 0x40000020u.toInt(),
        0x00000400u.toInt(), 0x00000020u.toInt(), 0x00100000u.toInt(), 0x40100400u.toInt(),
        0x40100020u.toInt(), 0x40000420u.toInt(), 0x00000420u.toInt(), 0x00000000u.toInt(),
        0x00000020u.toInt(), 0x40100000u.toInt(), 0x40000000u.toInt(), 0x00100020u.toInt(),
        0x00000000u.toInt(), 0x40100020u.toInt(), 0x00100020u.toInt(), 0x00000420u.toInt(),
        0x40000020u.toInt(), 0x00000400u.toInt(), 0x40100420u.toInt(), 0x00100000u.toInt(),
        0x00100420u.toInt(), 0x40000000u.toInt(), 0x40000400u.toInt(), 0x40100420u.toInt(),
        0x40100000u.toInt(), 0x00100420u.toInt(), 0x00100400u.toInt(), 0x40000400u.toInt()
    )

    private val LOOKUP6 = intArrayOf(
        0x00800000u.toInt(), 0x00001000u.toInt(), 0x00000040u.toInt(), 0x00801042u.toInt(),
        0x00801002u.toInt(), 0x00800040u.toInt(), 0x00001042u.toInt(), 0x00801000u.toInt(),
        0x00001000u.toInt(), 0x00000002u.toInt(), 0x00800002u.toInt(), 0x00001040u.toInt(),
        0x00800042u.toInt(), 0x00801002u.toInt(), 0x00801040u.toInt(), 0x00000000u.toInt(),
        0x00001040u.toInt(), 0x00800000u.toInt(), 0x00001002u.toInt(), 0x00000042u.toInt(),
        0x00800040u.toInt(), 0x00001042u.toInt(), 0x00000000u.toInt(), 0x00800002u.toInt(),
        0x00000002u.toInt(), 0x00800042u.toInt(), 0x00801042u.toInt(), 0x00001002u.toInt(),
        0x00801000u.toInt(), 0x00000040u.toInt(), 0x00000042u.toInt(), 0x00801040u.toInt(),
        0x00801040u.toInt(), 0x00800042u.toInt(), 0x00001002u.toInt(), 0x00801000u.toInt(),
        0x00001000u.toInt(), 0x00000002u.toInt(), 0x00800002u.toInt(), 0x00800040u.toInt(),
        0x00800000u.toInt(), 0x00001040u.toInt(), 0x00801042u.toInt(), 0x00000000u.toInt(),
        0x00001042u.toInt(), 0x00800000u.toInt(), 0x00000040u.toInt(), 0x00001002u.toInt(),
        0x00800042u.toInt(), 0x00000040u.toInt(), 0x00000000u.toInt(), 0x00801042u.toInt(),
        0x00801002u.toInt(), 0x00801040u.toInt(), 0x00000042u.toInt(), 0x00001000u.toInt(),
        0x00001040u.toInt(), 0x00801002u.toInt(), 0x00800040u.toInt(), 0x00000042u.toInt(),
        0x00000002u.toInt(), 0x00001042u.toInt(), 0x00801000u.toInt(), 0x00800002u.toInt()
    )

    private val LOOKUP7 = intArrayOf(
        0x10400000u.toInt(), 0x00404010u.toInt(), 0x00000010u.toInt(), 0x10400010u.toInt(),
        0x10004000u.toInt(), 0x00400000u.toInt(), 0x10400010u.toInt(), 0x00004010u.toInt(),
        0x00400010u.toInt(), 0x00004000u.toInt(), 0x00404000u.toInt(), 0x10000000u.toInt(),
        0x10404010u.toInt(), 0x10000010u.toInt(), 0x10000000u.toInt(), 0x10404000u.toInt(),
        0x00000000u.toInt(), 0x10004000u.toInt(), 0x00404010u.toInt(), 0x00000010u.toInt(),
        0x10000010u.toInt(), 0x10404010u.toInt(), 0x00004000u.toInt(), 0x10400000u.toInt(),
        0x10404000u.toInt(), 0x00400010u.toInt(), 0x10004010u.toInt(), 0x00404000u.toInt(),
        0x00004010u.toInt(), 0x00000000u.toInt(), 0x00400000u.toInt(), 0x10004010u.toInt(),
        0x00404010u.toInt(), 0x00000010u.toInt(), 0x10000000u.toInt(), 0x00004000u.toInt(),
        0x10000010u.toInt(), 0x10004000u.toInt(), 0x00404000u.toInt(), 0x10400010u.toInt(),
        0x00000000u.toInt(), 0x00404010u.toInt(), 0x00004010u.toInt(), 0x10404000u.toInt(),
        0x10004000u.toInt(), 0x00400000u.toInt(), 0x00404010u.toInt(), 0x10000000u.toInt(),
        0x10004010u.toInt(), 0x10400000u.toInt(), 0x00400000u.toInt(), 0x10404010u.toInt(),
        0x00004000u.toInt(), 0x00400010u.toInt(), 0x10400010u.toInt(), 0x00004010u.toInt(),
        0x00400010u.toInt(), 0x00000000u.toInt(), 0x10404000u.toInt(), 0x10000010u.toInt(),
        0x10400000u.toInt(), 0x10004010u.toInt(), 0x00000010u.toInt(), 0x00404000u.toInt()
    )

    private val LOOKUP8 = intArrayOf(
        0x00208080u.toInt(), 0x00008000u.toInt(), 0x20200000u.toInt(), 0x20208080u.toInt(),
        0x00200000u.toInt(), 0x20008080u.toInt(), 0x20008000u.toInt(), 0x20200000u.toInt(),
        0x20008080u.toInt(), 0x00208080u.toInt(), 0x00208000u.toInt(), 0x20000080u.toInt(),
        0x20200080u.toInt(), 0x00200000u.toInt(), 0x00000000u.toInt(), 0x20008000u.toInt(),
        0x00008000u.toInt(), 0x20000000u.toInt(), 0x00200080u.toInt(), 0x00008080u.toInt(),
        0x20208080u.toInt(), 0x00208000u.toInt(), 0x20000080u.toInt(), 0x00200080u.toInt(),
        0x20000000u.toInt(), 0x00000080u.toInt(), 0x00008080u.toInt(), 0x20208000u.toInt(),
        0x00000080u.toInt(), 0x20200080u.toInt(), 0x20208000u.toInt(), 0x00000000u.toInt(),
        0x00000000u.toInt(), 0x20208080u.toInt(), 0x00200080u.toInt(), 0x20008000u.toInt(),
        0x00208080u.toInt(), 0x00008000u.toInt(), 0x20000080u.toInt(), 0x00200080u.toInt(),
        0x20208000u.toInt(), 0x00000080u.toInt(), 0x00008080u.toInt(), 0x20200000u.toInt(),
        0x20008080u.toInt(), 0x20000000u.toInt(), 0x20200000u.toInt(), 0x00208000u.toInt(),
        0x20208080u.toInt(), 0x00008080u.toInt(), 0x00208000u.toInt(), 0x20200080u.toInt(),
        0x00200000u.toInt(), 0x20000080u.toInt(), 0x20008000u.toInt(), 0x00000000u.toInt(),
        0x00008000u.toInt(), 0x00200000u.toInt(), 0x20200080u.toInt(), 0x00208080u.toInt(),
        0x20000000u.toInt(), 0x20208000u.toInt(), 0x00000080u.toInt(), 0x20208080u.toInt()
    )
}
