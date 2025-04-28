package com.shepeliev.webrtckmp

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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

    private val events = MutableSharedFlow<DataChannelEvent>(
        extraBufferCapacity = Channel.UNLIMITED
    )

    init {
        android.registerObserver(DataChannelObserver())
    }

    actual val onOpen: Flow<Unit> = events
        .filter { it is DataChannelEvent.StateChanged && it.state == AndroidDataChannel.State.OPEN }
        .map { }

    actual val onClosing: Flow<Unit> = events
        .filter { it is DataChannelEvent.StateChanged && it.state == AndroidDataChannel.State.CLOSING }
        .map { }

    actual val onClose: Flow<Unit> = events
        .filter { it is DataChannelEvent.StateChanged && it.state == AndroidDataChannel.State.CLOSED }
        .map { }

    actual val onError: Flow<String> = emptyFlow()

    actual val onMessage: Flow<ByteArray> = events
        .map { it as? DataChannelEvent.MessageReceived }
        .filterNotNull()
        .map { it.buffer.data.toByteArray() }

    actual fun send(data: ByteArray): Boolean {
        val buffer = AndroidDataChannel.Buffer(ByteBuffer.wrap(data), true)
        return android.send(buffer)
    }

    actual fun close() {
        android.close()
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
        data class StateChanged(val state: AndroidDataChannel.State) : DataChannelEvent
        data class MessageReceived(val buffer: AndroidDataChannel.Buffer) : DataChannelEvent
    }

    private inner class DataChannelObserver : AndroidDataChannel.Observer {
        override fun onBufferedAmountChange(p0: Long) {
            // not implemented
        }

        override fun onStateChange() {
            val state = android.state()
            check(events.tryEmit(DataChannelEvent.StateChanged(state))) {
                // as we use SharedFlow with unlimited buffer, this should never happen
                "Failed to emit StateChanged event."
            }
            if (state == AndroidDataChannel.State.CLOSED) {
                android.unregisterObserver()
                android.dispose()
            }
        }

        override fun onMessage(buffer: AndroidDataChannel.Buffer) {
            check(events.tryEmit(DataChannelEvent.MessageReceived(buffer))) {
                // as we use SharedFlow with unlimited buffer, this should never happen
                "Failed to emit MessageReceived event"
            }
        }
    }
}
