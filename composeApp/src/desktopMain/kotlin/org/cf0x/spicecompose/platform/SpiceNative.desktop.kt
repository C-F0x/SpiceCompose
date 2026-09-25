package org.cf0x.spicecompose.platform

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Desktop bridge to the Rust C ABI, matching the Android JNI and iOS cinterop paths. */
actual object SpiceNative {
    private interface NativeApi : Library {
        fun spice_native_connect(host: String, port: Int, password: String): Byte
        fun spice_native_request(module: String, function: String, paramsJson: String): Pointer?
        fun spice_native_touch_request(module: String, function: String, paramsJson: String): Pointer?
        fun spice_native_disconnect()
        fun spice_native_last_error(): Pointer?
        fun spice_native_free_string(value: Pointer?)
    }

    private val nativeApi: NativeApi by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Native.load(resolveLibrary().absolutePath, NativeApi::class.java)
    }

    private fun resolveLibrary(): File {
        val libraryName = System.mapLibraryName("spice_backend")
        val configured = listOfNotNull(
            System.getProperty("spice.backend.library"),
            System.getenv("SPICE_BACKEND_LIBRARY"),
        ).map(::File).firstOrNull(File::isFile)
        if (configured != null) return configured

        val local = listOf(
            File("rust-backend/target/release", libraryName),
            File("rust-backend/target/debug", libraryName),
            File(System.getProperty("user.dir"), libraryName),
        ).firstOrNull(File::isFile)
        if (local != null) return local

        val resource = SpiceNative::class.java.getResourceAsStream("/$libraryName")
            ?: error("Rust desktop library not found; build the desktop target or set spice.backend.library")
        val extracted = Files.createTempFile("spicecompose-$libraryName-", "").toFile()
        extracted.deleteOnExit()
        resource.use { input ->
            Files.copy(input, extracted.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        return extracted
    }

    private fun readAndFree(pointer: Pointer?): String {
        return readNativeString(pointer) ?: "{\"error\":\"native returned null\"}"
    }

    private fun readNativeString(pointer: Pointer?): String? {
        if (pointer == null) return null
        return try {
            pointer.getString(0, Charsets.UTF_8.name())
        } finally {
            nativeApi.spice_native_free_string(pointer)
        }
    }

    actual suspend fun connect(host: String, port: Int, password: String): Boolean {
        if (host.isEmpty()) return false
        return withContext(Dispatchers.IO) {
            nativeApi.spice_native_connect(host, port, password).toInt() != 0
        }
    }

    actual suspend fun request(module: String, function: String, paramsJson: String): String =
        withContext(Dispatchers.IO) {
            readAndFree(nativeApi.spice_native_request(module, function, paramsJson))
        }

    actual suspend fun touchRequest(module: String, function: String, paramsJson: String): String =
        withContext(Dispatchers.IO) {
            readAndFree(nativeApi.spice_native_touch_request(module, function, paramsJson))
        }

    actual suspend fun disconnect() {
        withContext(Dispatchers.IO) { nativeApi.spice_native_disconnect() }
    }

    internal fun lastConnectError(): String = readNativeString(nativeApi.spice_native_last_error()).orEmpty()
}
