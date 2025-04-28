package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import java.nio.ByteBuffer
import org.webrtc.DataChannel as AndroidDataChannel

actual class DataChannel(val android: AndroidDataChannel) {

    actual val label: String
        get() = android.label()

    actual val id: Int
        get() = android.id()

    actual val readyState: DataChannelState
        get() = android.state().toCommon()

    actual val bufferedAmount: Long
        get() = android.bufferedAmount()

    private val _dataChannelEvent = MutableSharedFlow<DataChannelEvent>()

    private val dataChannelEvent: SharedFlow<DataChannelEvent> = _dataChannelEvent

    private val observer = object : AndroidDataChannel.Observer {
        override fun onBufferedAmountChange(p0: Long) {
            // not implemented
        }

        override fun onStateChange() {
            _dataChannelEvent.tryEmit(DataChannelEvent.StateChanged)
        }

        override fun onMessage(buffer: org.webrtc.DataChannel.Buffer) {
            _dataChannelEvent.tryEmit(DataChannelEvent.MessageReceived(buffer))
        }
    }

    init {
        android.registerObserver(observer)
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

    actual fun close() {
        android.unregisterObserver()
        android.dispose()
    }

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
