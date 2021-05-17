package com.shepeliev.webrtckmp

expect class DataChannel {
    val label: String
    val id: Int
    val state: DataChannelState
    val bufferedAmount: Long

    //TODO(shepeliev): DataChannel should expose event flows instead of observer's callbacks
    fun registerObserver(observer: DataChannelObserver)
    fun unregisterObserver()
    fun send(buffer: DataChannelBuffer): Boolean
    fun close()
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
