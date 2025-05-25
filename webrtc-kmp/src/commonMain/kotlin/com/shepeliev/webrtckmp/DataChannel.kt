package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.Flow

public expect class DataChannel {
    public val id: Int
    public val label: String
    public val readyState: DataChannelState
    public val bufferedAmount: Long

    public val onOpen: Flow<Unit>
    public val onClose: Flow<Unit>
    public val onClosing: Flow<Unit>
    public val onError: Flow<String>
    public val onMessage: Flow<ByteArray>

    public fun send(data: ByteArray): Boolean

    public fun close()
}

public enum class DataChannelState { Connecting, Open, Closing, Closed; }
