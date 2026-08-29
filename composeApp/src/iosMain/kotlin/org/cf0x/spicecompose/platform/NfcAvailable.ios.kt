package org.cf0x.spicecompose.platform

// Core NFC is available on supported physical devices.
// The reader checks runtime availability before starting.
actual val nfcAvailable: Boolean = true

// iPhone feedback is available.
actual val vibrationAvailable: Boolean = true
