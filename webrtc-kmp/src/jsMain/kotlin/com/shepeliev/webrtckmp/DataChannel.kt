package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.khronos.webgl.Int8Array

actual class DataChannel internal constructor(val js: RTCDataChannel) {
    actual val id: Int
        get() = js.id

    actual val label: String
        get() = js.label

    actual val readyState: DataChannelState
        get() = when (val readyState = js.readyState) {
            "connecting" -> DataChannelState.Connecting
            "open" -> DataChannelState.Open
            "closing" -> DataChannelState.Closing
            "closed" -> DataChannelState.Closed
            else -> throw IllegalArgumentException("Illegal ready state: $readyState")
        }

    actual val bufferedAmount: Long
        get() = js.bufferedAmount

    private val _onOpen = MutableSharedFlow<Unit>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onOpen: Flow<Unit> = _onOpen.asSharedFlow()

    private val _onClosing = MutableSharedFlow<Unit>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onClosing: Flow<Unit> = _onClosing.asSharedFlow()

    private val _onClose = MutableSharedFlow<Unit>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onClose: Flow<Unit> = _onClose.asSharedFlow()

    private val _onError = MutableSharedFlow<String>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onError: Flow<String> = _onError.asSharedFlow()

    private val _onMessage = MutableSharedFlow<ByteArray>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    actual val onMessage: Flow<ByteArray> = _onMessage.asSharedFlow()

    init {
        js.onopen = { _onOpen.tryEmit(Unit) }
        js.onclosing = { _onClosing.tryEmit(Unit) }
        js.onclose = { _onClose.tryEmit(Unit) }
        js.onerror = { _onError.tryEmit(it.message) }
        js.onmessage = {
            _onMessage.tryEmit(Int8Array(it.data).unsafeCast<ByteArray>())
        }
    }

    actual fun send(data: ByteArray): Boolean {
        val conversion = data.unsafeCast<Int8Array>()
        js.send(conversion)
        return true
    }

    actual fun close() {
        js.close()
    }
}
