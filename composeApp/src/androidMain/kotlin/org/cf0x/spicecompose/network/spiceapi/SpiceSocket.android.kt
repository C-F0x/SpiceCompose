package org.cf0x.spicecompose.network.spiceapi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

actual class SpiceSocket {
    private var socket: Socket? = null

    actual suspend fun connect(host: String, port: Int, timeout: Long) {
        withContext(Dispatchers.IO) {
            val s = Socket()
            s.connect(InetSocketAddress(host, port), timeout.toInt())
            socket = s
        }
    }

    actual suspend fun read(): ByteArray? = withContext(Dispatchers.IO) {
        val s = socket ?: return@withContext null
        val buffer = ByteArray(8192)
        val read = s.getInputStream().read(buffer)
        if (read == -1) return@withContext null
        buffer.copyOfRange(0, read)
    }

    actual suspend fun write(data: ByteArray) {
        withContext(Dispatchers.IO) {
            socket?.getOutputStream()?.write(data)
        }
    }

    actual fun close() {
        socket?.close()
    }
}
