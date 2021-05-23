package com.shepeliev.webrtckmp

import WebRTC.RTCDataBuffer
import WebRTC.RTCDataChannel
import WebRTC.RTCDataChannelDelegateProtocol
import WebRTC.RTCDataChannelState
import com.shepeliev.webrtckmp.ios.toByteArray
import com.shepeliev.webrtckmp.ios.toNSData
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import platform.darwin.NSObject
import platform.posix.uint64_t
import kotlin.native.concurrent.freeze

actual class DataChannel(val native: RTCDataChannel) {

    actual val label: String
        get() = native.label()

    actual val id: Int
        get() = native.channelId

    actual val readyState: DataChannelState
        get() = rtcDataChannelStateAsCommon(native.readyState())

    actual val bufferedAmount: Long
        get() = native.bufferedAmount.toLong()

    private val onOpenInternal = MutableSharedFlow<Unit>()
    actual val onOpen: Flow<Unit> = onOpenInternal.asSharedFlow()

    private val onClosingInternal = MutableSharedFlow<Unit>()
    actual val onClosing: Flow<Unit> = onClosingInternal.asSharedFlow()

    private val onCloseInternal = MutableSharedFlow<Unit>()
    actual val onClose: Flow<Unit> = onCloseInternal.asSharedFlow()

    private val onErrorInternal = MutableSharedFlow<Throwable>()
    actual val onError: Flow<Throwable> = onErrorInternal.asSharedFlow()

    private val onMessageInternal = MutableSharedFlow<ByteArray>()
    actual val onMessage: Flow<ByteArray> = onMessageInternal.asSharedFlow()

    private val scope = MainScope()

    init {
        val delegate = object : NSObject(), RTCDataChannelDelegateProtocol {
            override fun dataChannel(
                dataChannel: RTCDataChannel,
                didChangeBufferedAmount: uint64_t
            ) {
                // not implemented
            }

            override fun dataChannel(
                dataChannel: RTCDataChannel,
                didReceiveMessageWithBuffer: RTCDataBuffer
            ) {
                val data = didReceiveMessageWithBuffer.data.toByteArray().freeze()
                scope.launch { onMessageInternal.emit(data) }
            }

            override fun dataChannelDidChangeState(dataChannel: RTCDataChannel) {
                scope.launch {
                    when (native.readyState) {
                        RTCDataChannelState.RTCDataChannelStateOpen -> onOpenInternal.emit(Unit)
                        RTCDataChannelState.RTCDataChannelStateClosing -> onClosingInternal.emit(
                            Unit
                        )
                        RTCDataChannelState.RTCDataChannelStateClosed -> {
                            onCloseInternal.emit(Unit)
                            scope.cancel()
                        }
                        else -> {
                            // ignore
                        }
                    }
                }
            }
        }
        native.delegate = delegate.freeze()
    }

    actual fun send(data: ByteArray): Boolean {
        val buffer = RTCDataBuffer(data.toNSData(), true).freeze()
        return native.sendData(buffer)
    }

    actual fun close() = native.close()
}

private fun rtcDataChannelStateAsCommon(state: RTCDataChannelState): DataChannelState {
    return when (state) {
        RTCDataChannelState.RTCDataChannelStateConnecting -> DataChannelState.Connecting
        RTCDataChannelState.RTCDataChannelStateOpen -> DataChannelState.Open
        RTCDataChannelState.RTCDataChannelStateClosing -> DataChannelState.Closing
        RTCDataChannelState.RTCDataChannelStateClosed -> DataChannelState.Closed
    }
}
