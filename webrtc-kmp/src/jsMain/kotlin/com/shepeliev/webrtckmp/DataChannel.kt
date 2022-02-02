package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

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
        js.onopen = { check(_onOpen.tryEmit(Unit)) {"onOpen"} }
        js.onclosing = { check(_onClosing.tryEmit(Unit)) {"onCLosing"} }
        js.onclose = { check(_onClose.tryEmit(Unit)) {"onClose"} }
        js.onerror = { check(_onError.tryEmit(it.message)) {"onError"} }
        js.onmessage = { check(_onMessage.tryEmit(it.data.encodeToByteArray())) { "onMessage" } }
    }

    actual fun send(data: ByteArray): Boolean {
        js.send(data.decodeToString())
        return true
    }

    actual fun close() {
        js.close()
    }
}

// TODO: the value is just an assumption and might be adjusted
private const val FLOW_BUFFER_CAPACITY = 42
