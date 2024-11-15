package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCDataChannelBuffer
import dev.onvoid.webrtc.RTCDataChannelObserver
import dev.onvoid.webrtc.RTCDataChannelState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import java.nio.ByteBuffer
import dev.onvoid.webrtc.RTCDataChannel as NativeDataChannel

actual class DataChannel(val native: NativeDataChannel) {

    actual val label: String
        get() = native.label

    actual val id: Int
        get() = native.id

    actual val readyState: DataChannelState
        get() = native.state.toCommon()

    actual val bufferedAmount: Long
        get() = native.bufferedAmount

    private val dataChannelEvent = callbackFlow {
        val observer = object : RTCDataChannelObserver {
            override fun onBufferedAmountChange(p0: Long) {
                // not implemented
            }

            override fun onStateChange() {
                trySendBlocking(DataChannelEvent.StateChanged)
            }

            override fun onMessage(buffer: RTCDataChannelBuffer) {
                trySendBlocking(DataChannelEvent.MessageReceived(buffer))
            }
        }

        native.registerObserver(observer)

        awaitClose { native.unregisterObserver() }
    }

    actual val onOpen: Flow<Unit> = dataChannelEvent
        .filter { it is DataChannelEvent.StateChanged && native.state == RTCDataChannelState.OPEN }
        .map { }

    actual val onClosing: Flow<Unit> = dataChannelEvent
        .filter { it is DataChannelEvent.StateChanged && native.state == RTCDataChannelState.CLOSING }
        .map { }

    actual val onClose: Flow<Unit> = dataChannelEvent
        .filter { it is DataChannelEvent.StateChanged && native.state == RTCDataChannelState.CLOSED }
        .map { }

    actual val onError: Flow<String> = emptyFlow()

    actual val onMessage: Flow<ByteArray> = dataChannelEvent
        .map { it as? DataChannelEvent.MessageReceived }
        .filterNotNull()
        .map { it.buffer.data.toByteArray() }

    actual fun send(data: ByteArray): Boolean {
        val buffer = RTCDataChannelBuffer(ByteBuffer.wrap(data), true)
        native.send(buffer)
        return true
    }

    actual fun close() = native.dispose()

    private fun RTCDataChannelState.toCommon(): DataChannelState {
        return when (this) {
            RTCDataChannelState.CONNECTING -> DataChannelState.Connecting
            RTCDataChannelState.OPEN -> DataChannelState.Open
            RTCDataChannelState.CLOSING -> DataChannelState.Closing
            RTCDataChannelState.CLOSED -> DataChannelState.Closed
        }
    }

    private sealed interface DataChannelEvent {
        object StateChanged : DataChannelEvent
        data class MessageReceived(val buffer: RTCDataChannelBuffer) : DataChannelEvent
    }
}
