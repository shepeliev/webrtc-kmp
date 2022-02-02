package com.shepeliev.webrtckmp

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.khronos.webgl.Uint8Array

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

    private val onOpenInternal = MutableSharedFlow<Unit>()
    actual val onOpen: Flow<Unit> = onOpenInternal.asSharedFlow()

    private val onClosingInternal = MutableSharedFlow<Unit>()
    actual val onClosing: Flow<Unit> = onClosingInternal.asSharedFlow()

    private val onCloseInternal = MutableSharedFlow<Unit>()
    actual val onClose: Flow<Unit> = onCloseInternal.asSharedFlow()

    private val onErrorInternal = MutableSharedFlow<String>()
    actual val onError: Flow<String> = onErrorInternal.asSharedFlow()

    private val onMessageInternal = MutableSharedFlow<ByteArray>()
    actual val onMessage: Flow<ByteArray> = onMessageInternal.asSharedFlow()

    private val scope = MainScope()

    init {
        js.onopen = { scope.launch { onOpenInternal.emit(Unit) } }
        js.onclosing = { scope.launch { onClosingInternal.emit(Unit) } }
        js.onclose = {
            scope.launch {
                onCloseInternal.emit(Unit)
                scope.cancel()
            }
        }
        js.onerror = { scope.launch { onErrorInternal.emit(it.message) } }
        js.onmessage = { scope.launch { onMessageInternal.emit(it.data.encodeToByteArray()) } }
    }

    actual fun send(data: ByteArray): Boolean {
        val bytes = Uint8Array(data.toTypedArray())
        js.send(data.decodeToString())
        return true
    }

    actual fun close() {
        js.close()
    }
}
