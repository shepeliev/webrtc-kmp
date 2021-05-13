package com.shepeliev.webrtckmp

import WebRTC.RTCDataBuffer
import WebRTC.RTCDataChannel
import WebRTC.RTCDataChannelDelegateProtocol
import WebRTC.RTCDataChannelState
import platform.darwin.NSObject
import platform.posix.uint64_t
import kotlin.native.concurrent.freeze

actual class DataChannel(val native: RTCDataChannel) {

    actual val label: String
        get() = native.label()

    actual val id: Int
        get() = native.channelId

    actual val state: DataChannelState
        get() = rtcDataChannelStateAsCommon(native.readyState())

    actual val bufferedAmount: Long
        get() = native.bufferedAmount.toLong()

    actual fun registerObserver(observer: DataChannelObserver) {
        val frozenObserver = observer.freeze()
        val delegate = object : NSObject(), RTCDataChannelDelegateProtocol {
            override fun dataChannel(
                dataChannel: RTCDataChannel,
                didChangeBufferedAmount: uint64_t
            ) {
                frozenObserver.onBufferedAmountChange(didChangeBufferedAmount.toLong())
            }

            override fun dataChannel(
                dataChannel: RTCDataChannel,
                didReceiveMessageWithBuffer: RTCDataBuffer
            ) {
                frozenObserver.onMessage(DataChannelBuffer(didReceiveMessageWithBuffer).freeze())
            }

            override fun dataChannelDidChangeState(dataChannel: RTCDataChannel) {
                frozenObserver.onStateChange()
            }
        }
        native.delegate = delegate.freeze()
    }

    actual fun unregisterObserver() {
        native.delegate = null
    }

    actual fun send(buffer: DataChannelBuffer): Boolean = native.sendData(buffer.native)
    actual fun close() = native.close()
}

actual class DataChannelBuffer internal constructor(val native: RTCDataBuffer){

    actual constructor(data: ByteArray, binary: Boolean):
        this(RTCDataBuffer(data.toNSData(), binary))

    actual val data: ByteArray
        get() = native.data.toByteArray()

    actual val binary: Boolean
        get() = native.isBinary
}

private fun rtcDataChannelStateAsCommon(state: RTCDataChannelState): DataChannelState {
    return when(state) {
        RTCDataChannelState.RTCDataChannelStateConnecting -> DataChannelState.Connecting
        RTCDataChannelState.RTCDataChannelStateOpen -> DataChannelState.Open
        RTCDataChannelState.RTCDataChannelStateClosing -> DataChannelState.Closing
        RTCDataChannelState.RTCDataChannelStateClosed -> DataChannelState.Closed
    }
}
