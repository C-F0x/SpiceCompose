package org.cf0x.spicecompose

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSNetServiceBrowser
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time

/** Triggers the iOS local-network prompt with a short Bonjour browse. */
@OptIn(ExperimentalForeignApi::class)
private object LocalNetworkPermission {
    private var browser: NSNetServiceBrowser? = null

    fun request() {
        if (browser != null) return
        val b = NSNetServiceBrowser()
        browser = b
        b.searchForServicesOfType("_http._tcp", inDomain = "local.")

        // Stop browsing after three seconds.
        val delta = 3_000_000_000L // nanoseconds
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, delta), dispatch_get_main_queue()) {
            b.stop()
            if (browser === b) browser = null
        }
    }
}

/** Requests local-network access once. */
fun requestLocalNetworkPermission() = LocalNetworkPermission.request()
