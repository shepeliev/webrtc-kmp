package com.shepeliev.apprtckmm.shared.rtcclient

expect class WebSocketConnection() {
    suspend fun connect(url: String, onFrame: suspend (WebSocketFrame) -> Unit)
    suspend fun sendTextMessage(message: String)
    suspend fun disconnect()
}

sealed class WebSocketFrame {
    data class Close(val code: String?, val message: String): WebSocketFrame()
    data class Text(val message: String): WebSocketFrame()
}
