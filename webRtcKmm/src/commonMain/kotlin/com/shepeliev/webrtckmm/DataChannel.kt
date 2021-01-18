package com.shepeliev.webrtckmm

// TODO implement initialization
expect class DataChannel {
    val label: String
    val id: Int
    val state: DataChannelState
    val bufferedAmount: Long

    fun registerObserver(observer: DataChannelObserver)
    fun unregisterObserver()
    fun send(buffer: DataChannelBuffer): Boolean
    fun close()
    fun dispose()
}

enum class DataChannelState { Connecting, Open, Closing, Closed; }

expect class DataChannelBuffer(data: ByteArray, binary: Boolean) {
    val data: ByteArray
    val binary: Boolean
}

interface DataChannelObserver {
    fun onBufferedAmountChange(previousAmount: Long)
    fun onStateChange()
    fun onMessage(buffer: DataChannelBuffer)
}
