package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCDataChannel
import dev.onvoid.webrtc.RTCDataChannelBuffer
import dev.onvoid.webrtc.RTCDataChannelObserver
import dev.onvoid.webrtc.RTCDataChannelState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import java.nio.ByteBuffer

actual class DataChannel(private val jvm: RTCDataChannel) {

    actual val label: String
        get() = jvm.label

    actual val id: Int
        get() = jvm.id

    actual val readyState: DataChannelState
        get() = jvm.state.asCommon()

    actual val bufferedAmount: Long
        get() = jvm.bufferedAmount

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

        jvm.registerObserver(observer)

        awaitClose { jvm.unregisterObserver() }
    }

    actual val onOpen: Flow<Unit> = dataChannelEvent
        .filter { it is DataChannelEvent.StateChanged && jvm.state == RTCDataChannelState.OPEN }
        .map { }

    actual val onClosing: Flow<Unit> = dataChannelEvent
        .filter { it is DataChannelEvent.StateChanged && jvm.state == RTCDataChannelState.CLOSING }
        .map { }

    actual val onClose: Flow<Unit> = dataChannelEvent
        .filter { it is DataChannelEvent.StateChanged && jvm.state == RTCDataChannelState.CLOSED }
        .map { }

    private val errorFlow: MutableStateFlow<String> = MutableStateFlow("")

    actual val onError: Flow<String> = errorFlow

    actual val onMessage: Flow<ByteArray> = dataChannelEvent
        .map { it as? DataChannelEvent.MessageReceived }
        .filterNotNull()
        .map { it.buffer.data.toByteArray() }

    actual fun send(data: ByteArray): Boolean {
        val buffer = RTCDataChannelBuffer(ByteBuffer.wrap(data), true)
        return try {
            jvm.send(buffer)
            true
        } catch (e: Exception) {
            errorFlow.tryEmit(e.message.orEmpty())
            false
        }
    }

    actual fun close() = jvm.dispose()

    private fun RTCDataChannelState.asCommon(): DataChannelState {
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
