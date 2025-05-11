package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.RTCDataChannel
import com.shepeliev.webrtckmp.externals.data
import com.shepeliev.webrtckmp.externals.send
import com.shepeliev.webrtckmp.internal.Console
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

actual class DataChannel internal constructor(internal val js: RTCDataChannel) {
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

    private val _onOpen = MutableSharedFlow<Unit>(extraBufferCapacity = Channel.UNLIMITED)
    actual val onOpen: Flow<Unit> = _onOpen.asSharedFlow()

    private val _onClosing = MutableSharedFlow<Unit>(extraBufferCapacity = Channel.UNLIMITED)
    actual val onClosing: Flow<Unit> = _onClosing.asSharedFlow()

    private val _onClose = MutableSharedFlow<Unit>(extraBufferCapacity = Channel.UNLIMITED)
    actual val onClose: Flow<Unit> = _onClose.asSharedFlow()

    private val _onError = MutableSharedFlow<String>(extraBufferCapacity = Channel.UNLIMITED)
    actual val onError: Flow<String> = _onError.asSharedFlow()

    private val _onMessage = MutableSharedFlow<ByteArray>(extraBufferCapacity = Channel.UNLIMITED)
    actual val onMessage: Flow<ByteArray> = _onMessage.asSharedFlow()

    init {
        js.onopen = { tryEmit(_onOpen, Unit) }
        js.onclosing = { tryEmit(_onClosing, Unit) }
        js.onerror = { tryEmit(_onError, it.message) }
        js.onmessage = { tryEmit(_onMessage, it.data) }
        js.onclose = { tryEmit(_onClose, Unit) }
    }

    private fun <T> tryEmit(flow: MutableSharedFlow<T>, event: T) {
        check(flow.tryEmit(event)) {
            // as we use SharedFlow with unlimited buffer, this should never happen
            "Failed to emit event: $event"
        }
    }

    actual fun send(data: ByteArray): Boolean {
        return runCatching { js.send(data) }
            .onFailure { Console.error("Failed to send data: $it") }
            .map { true }
            .getOrDefault(false)
    }

    actual fun close() {
        js.close()
    }
}
