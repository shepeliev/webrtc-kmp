package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.utils.toByteArray
import java.nio.ByteBuffer
import org.webrtc.DataChannel as NativeDataChannel

actual class DataChannel(val native: NativeDataChannel) {

    actual val label: String
        get() = native.label()

    actual val id: Int
        get() = native.id()

    actual val state: DataChannelState
        get() = native.state().toCommon()

    actual val bufferedAmount: Long
        get() = native.bufferedAmount()

    actual fun registerObserver(observer: DataChannelObserver) {
        native.registerObserver(object : NativeDataChannel.Observer {
            override fun onBufferedAmountChange(previousAmount: Long) {
                observer.onBufferedAmountChange(previousAmount)
            }

            override fun onStateChange() {
                observer.onStateChange()
            }

            override fun onMessage(buffer: NativeDataChannel.Buffer) {
                observer.onMessage(DataChannelBuffer(buffer))
            }
        })
    }

    actual fun unregisterObserver() {
        native.unregisterObserver()
    }

    actual fun send(buffer: DataChannelBuffer): Boolean = native.send(buffer.native)
    actual fun close() = native.dispose()

    private fun NativeDataChannel.State.toCommon(): DataChannelState {
        return when(this) {
            NativeDataChannel.State.CONNECTING -> DataChannelState.Connecting
            NativeDataChannel.State.OPEN -> DataChannelState.Open
            NativeDataChannel.State.CLOSING -> DataChannelState.Closing
            NativeDataChannel.State.CLOSED -> DataChannelState.Closed
        }
    }
}

actual class DataChannelBuffer internal constructor(val native: NativeDataChannel.Buffer){

    actual constructor(data: ByteArray, binary: Boolean):
        this(NativeDataChannel.Buffer(ByteBuffer.wrap(data), binary))

    actual val data: ByteArray
        get() = native.data.toByteArray()

    actual val binary: Boolean
        get() = native.binary
}
