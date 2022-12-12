package com.shepeliev.webrtckmp

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import java.nio.ByteBuffer
actual class DataChannel(val android: AndroidDataChannel) {

    actual val label: String
        get() = android.label()

    actual val id: Int
        get() = android.id()

    actual val readyState: DataChannelState
        get() = android.state().toCommon()

    actual val bufferedAmount: Long
        get() = android.bufferedAmount()

    private val dataChannelEvent = callbackFlow {
        val observer = object : AndroidDataChannel.Observer {
            override fun onBufferedAmountChange(p0: Long) {
                // not implemented
            }

            override fun onStateChange() {
                trySendBlocking(DataChannelEvent.StateChanged)
            }

            override fun onMessage(buffer: org.webrtc.DataChannel.Buffer) {
                trySendBlocking(DataChannelEvent.MessageReceived(buffer))
            }
        }

        android.registerObserver(observer)

        awaitClose { android.unregisterObserver() }
    }

    actual val onOpen: Flow<Unit> = dataChannelEvent
        .filter { it is DataChannelEvent.StateChanged && android.state() == AndroidDataChannel.State.OPEN }
        .map { }

    actual val onClosing: Flow<Unit> = dataChannelEvent
        .filter { it is DataChannelEvent.StateChanged && android.state() == AndroidDataChannel.State.CLOSING }
        .map { }

    actual val onClose: Flow<Unit> = dataChannelEvent
        .filter { it is DataChannelEvent.StateChanged && android.state() == AndroidDataChannel.State.CLOSED }
        .map { }

    actual val onError: Flow<String> = emptyFlow()

    actual val onMessage: Flow<ByteArray> = dataChannelEvent
        .map { it as? DataChannelEvent.MessageReceived }
        .filterNotNull()
        .map { it.buffer.data.toByteArray() }

    actual fun send(data: ByteArray): Boolean {
        val buffer = AndroidDataChannel.Buffer(ByteBuffer.wrap(data), true)
        return android.send(buffer)
    }

    actual fun close() = android.dispose()

    private fun AndroidDataChannel.State.toCommon(): DataChannelState {
        return when (this) {
            AndroidDataChannel.State.CONNECTING -> DataChannelState.Connecting
            AndroidDataChannel.State.OPEN -> DataChannelState.Open
            AndroidDataChannel.State.CLOSING -> DataChannelState.Closing
            AndroidDataChannel.State.CLOSED -> DataChannelState.Closed
        }
    }

    private sealed interface DataChannelEvent {
        object StateChanged : DataChannelEvent
        data class MessageReceived(val buffer: AndroidDataChannel.Buffer) : DataChannelEvent
    }
}
