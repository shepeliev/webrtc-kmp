package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.Flow

expect class DataChannel {
    val id: Int
    val label: String
    val readyState: DataChannelState
    val bufferedAmount: Long

    val onOpen: Flow<Unit>
    val onClose: Flow<Unit>
    val onClosing: Flow<Unit>
    val onError: Flow<String>
    val onMessage: Flow<ByteArray>

    fun send(data: ByteArray): Boolean
    fun close()
}

enum class DataChannelState { Connecting, Open, Closing, Closed; }
