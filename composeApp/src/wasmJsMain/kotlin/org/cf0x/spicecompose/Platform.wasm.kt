package org.cf0x.spicecompose

actual fun getPlatform(): Platform = object : Platform {
    override val name: String = "Web/Wasm"
}
