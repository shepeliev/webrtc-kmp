package com.shepeliev.apprtckmm.shared.rtcclient

actual class WebSocketConnection {
    actual suspend fun connect(url: String, onFrame: suspend (WebSocketFrame) -> Unit) {
        TODO()
    }

    actual suspend fun sendTextMessage(message: String) {
        TODO()
    }

    actual suspend fun disconnect() {
        TODO()
    }
}