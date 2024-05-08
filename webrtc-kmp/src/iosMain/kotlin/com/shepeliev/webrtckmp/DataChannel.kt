@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCDataBuffer
import WebRTC.RTCDataChannel
import WebRTC.RTCDataChannelDelegateProtocol
import WebRTC.RTCDataChannelState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.uint64_t

actual class DataChannel(val ios: RTCDataChannel) {
    actual val label: String
        get() = ios.label()

    actual val id: Int
        get() = ios.channelId

    actual val readyState: DataChannelState
        get() = rtcDataChannelStateAsCommon(ios.readyState())

    actual val bufferedAmount: Long
        get() = ios.bufferedAmount.toLong()

    private val coroutineScope = MainScope()
    private val dataChannelEvent = MutableSharedFlow<DataChannelEvent>()

    init {
        ios.delegate = object : NSObject(), RTCDataChannelDelegateProtocol {
            override fun dataChannel(dataChannel: RTCDataChannel, didChangeBufferedAmount: uint64_t) {
                // not implemented
            }

            override fun dataChannel(dataChannel: RTCDataChannel, didReceiveMessageWithBuffer: RTCDataBuffer) {
                coroutineScope.launch {
                    dataChannelEvent.emit(DataChannelEvent.MessageReceived(didReceiveMessageWithBuffer))
                }
            }

            override fun dataChannelDidChangeState(dataChannel: RTCDataChannel) {
                coroutineScope.launch { dataChannelEvent.emit(DataChannelEvent.StateChanged) }
            }
        }
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
        val buffer = RTCDataBuffer(data.toNSData(), true)
        return ios.sendData(buffer)
    }

    actual fun close() {
        ios.close()
        coroutineScope.launch {
            dataChannelEvent.emit(DataChannelEvent.StateChanged)
            coroutineScope.cancel()
        }
    }

    private sealed interface DataChannelEvent {
        data object StateChanged : DataChannelEvent
        data class MessageReceived(val buffer: RTCDataBuffer) : DataChannelEvent
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
}

private class ProxyDataChannelDelegate : NSObject(), RTCDataChannelDelegateProtocol {
    private val delegates: MutableSet<RTCDataChannelDelegateProtocol> = mutableSetOf()

    fun addDelegate(delegate: RTCDataChannelDelegateProtocol) {
        dispatch_async(dispatch_get_main_queue()) {
            delegates.add(delegate)
        }
    }

    fun removeDelegate(delegate: RTCDataChannelDelegateProtocol) {
        dispatch_async(dispatch_get_main_queue()) {
            delegates.remove(delegate)
        }
    }

    override fun dataChannel(dataChannel: RTCDataChannel, didChangeBufferedAmount: uint64_t) {
        dispatch_async(dispatch_get_main_queue()) {
            delegates.forEach { it.dataChannel(dataChannel, didChangeBufferedAmount) }
        }
    }

    override fun dataChannel(dataChannel: RTCDataChannel, didReceiveMessageWithBuffer: RTCDataBuffer) {
        dispatch_async(dispatch_get_main_queue()) {
            delegates.forEach { it.dataChannel(dataChannel, didReceiveMessageWithBuffer) }
        }
    }

    override fun dataChannelDidChangeState(dataChannel: RTCDataChannel) {
        dispatch_async(dispatch_get_main_queue()) {
            delegates.forEach { it.dataChannelDidChangeState(dataChannel) }
        }
    }
}
