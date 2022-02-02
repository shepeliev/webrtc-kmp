package com.shepeliev.webrtckmp

import WebRTC.RTCDataBuffer
import WebRTC.RTCDataChannel
import WebRTC.RTCDataChannelDelegateProtocol
import WebRTC.RTCDataChannelState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import platform.darwin.NSObject
import platform.posix.uint64_t
import kotlin.native.concurrent.freeze

actual class DataChannel(val ios: RTCDataChannel) {

    actual val label: String
        get() = ios.label()

    actual val id: Int
        get() = ios.channelId

    actual val readyState: DataChannelState
        get() = rtcDataChannelStateAsCommon(ios.readyState())

    actual val bufferedAmount: Long
        get() = ios.bufferedAmount.toLong()

    private val dataChannelEvent = callbackFlow {
        ios.delegate = object : NSObject(), RTCDataChannelDelegateProtocol {
            override fun dataChannel(dataChannel: RTCDataChannel, didChangeBufferedAmount: uint64_t) {
                // not implemented
            }

            override fun dataChannel(dataChannel: RTCDataChannel, didReceiveMessageWithBuffer: RTCDataBuffer) {
                trySendBlocking(DataChannelEvent.MessageReceived(didReceiveMessageWithBuffer))
            }

            override fun dataChannelDidChangeState(dataChannel: RTCDataChannel) {
                trySendBlocking(DataChannelEvent.StateChanged)
            }
        }

        awaitClose { ios.delegate = null }
    }

    actual val onOpen: Flow<Unit> = dataChannelEvent
        .filter { it is DataChannelEvent.StateChanged && ios.readyState == RTCDataChannelState.RTCDataChannelStateOpen }
        .map { }

    actual val onClosing: Flow<Unit> = dataChannelEvent
        .filter { it is DataChannelEvent.StateChanged && ios.readyState == RTCDataChannelState.RTCDataChannelStateClosing }
        .map { }


    actual val onClose: Flow<Unit> = dataChannelEvent
        .filter { it is DataChannelEvent.StateChanged && ios.readyState == RTCDataChannelState.RTCDataChannelStateClosed }
        .map { }

    actual val onError: Flow<String> = emptyFlow()

    actual val onMessage: Flow<ByteArray> = dataChannelEvent
        .map { it as? DataChannelEvent.MessageReceived }
        .filterNotNull()
        .map { it.buffer.data.toByteArray() }

    actual fun send(data: ByteArray): Boolean {
        val buffer = RTCDataBuffer(data.toNSData(), true).freeze()
        return ios.sendData(buffer)
    }

    actual fun close() = ios.close()

    private sealed interface DataChannelEvent {
        object StateChanged : DataChannelEvent
        data class MessageReceived(val buffer: RTCDataBuffer) : DataChannelEvent
    }
}

private fun rtcDataChannelStateAsCommon(state: RTCDataChannelState): DataChannelState {
    return when (state) {
        RTCDataChannelState.RTCDataChannelStateConnecting -> DataChannelState.Connecting
        RTCDataChannelState.RTCDataChannelStateOpen -> DataChannelState.Open
        RTCDataChannelState.RTCDataChannelStateClosing -> DataChannelState.Closing
        RTCDataChannelState.RTCDataChannelStateClosed -> DataChannelState.Closed
        else -> error("Unknown RTCDataChannelState: $state")
    }
}
