package org.cf0x.spicecompose.platform

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

expect val nfcAvailable: Boolean
expect val vibrationAvailable: Boolean

object NfcManager {
    private val _tagIdFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val tagIdFlow = _tagIdFlow.asSharedFlow()

    fun onTagDiscovered(id: String) {
        _tagIdFlow.tryEmit(id)
    }
}
