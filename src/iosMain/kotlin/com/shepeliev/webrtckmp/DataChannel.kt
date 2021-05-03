package com.shepeliev.webrtckmp

import WebRTC.RTCDataBuffer
import WebRTC.RTCDataChannel
import WebRTC.RTCDataChannelDelegateProtocol
import WebRTC.RTCDataChannelState
import platform.darwin.NSObject

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
        native.delegate = object : NSObject(), RTCDataChannelDelegateProtocol {
            override fun dataChannel(
                dataChannel: RTCDataChannel,
                didReceiveMessageWithBuffer: RTCDataBuffer
            ) {
                observer.onMessage(DataChannelBuffer(didReceiveMessageWithBuffer))
            }

            override fun dataChannelDidChangeState(dataChannel: RTCDataChannel) {
                observer.onStateChange()
            }
        }
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
