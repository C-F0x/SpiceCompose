package org.cf0x.spicecompose

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform