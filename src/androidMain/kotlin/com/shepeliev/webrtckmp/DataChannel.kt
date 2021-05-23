package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.utils.toByteArray
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import org.webrtc.DataChannel as AndroidDataChannel

actual class DataChannel(val native: AndroidDataChannel) : AndroidDataChannel.Observer {

    actual val label: String
        get() = native.label()

    actual val id: Int
        get() = native.id()

    actual val readyState: DataChannelState
        get() = native.state().toCommon()

    actual val bufferedAmount: Long
        get() = native.bufferedAmount()

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
        native.registerObserver(this)
    }

    actual fun send(data: ByteArray): Boolean {
        val buffer = AndroidDataChannel.Buffer(ByteBuffer.wrap(data), true)
        return native.send(buffer)
    }

    actual fun close() = native.dispose()

    private fun AndroidDataChannel.State.toCommon(): DataChannelState {
        return when (this) {
            AndroidDataChannel.State.CONNECTING -> DataChannelState.Connecting
            AndroidDataChannel.State.OPEN -> DataChannelState.Open
            AndroidDataChannel.State.CLOSING -> DataChannelState.Closing
            AndroidDataChannel.State.CLOSED -> DataChannelState.Closed
        }
    }

    override fun onBufferedAmountChange(reviousAmount: Long) {
        // not implemented
    }

    override fun onStateChange() {
        scope.launch {
            when (native.state()) {
                AndroidDataChannel.State.OPEN -> onOpenInternal.emit(Unit)
                AndroidDataChannel.State.CLOSING -> onClosingInternal.emit(Unit)
                AndroidDataChannel.State.CLOSED -> {
                    onCloseInternal.emit(Unit)
                    scope.cancel()
                }
                else -> {
                    // ignore
                }
            }
        }
    }

    override fun onMessage(buffer: AndroidDataChannel.Buffer) {
        val data = buffer.data.toByteArray()
        scope.launch { onMessageInternal.emit(data) }
    }
}
