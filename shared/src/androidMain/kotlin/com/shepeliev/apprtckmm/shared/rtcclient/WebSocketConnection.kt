package com.shepeliev.apprtckmm.shared.rtcclient

import com.shepeliev.apprtckmm.shared.Log
import io.ktor.client.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

actual class WebSocketConnection {

    private val tag = "WSConnection"
    private val client = HttpClient {
        install(WebSockets)
        install(Logging)
    }

    private var wsSession: WebSocketSession? = null

    actual suspend fun connect(url: String, onFrame: suspend (WebSocketFrame) -> Unit) {
        Log.d(tag, "Connect to $url")
        val session = client.webSocketSession {
            url(url)
            header("Origin", "https://appr.tc")
        }
        wsSession = session

        MainScope().launch {
            while (true) {
                session.incoming.receiveAsFlow().collect {
                    when (it) {
                        is Frame.Text -> onFrame(WebSocketFrame.Text(it.readText()))
                        is Frame.Close -> {
                            val reason = it.readReason()
                            val code = reason?.knownReason?.toString()
                            val message = reason?.message ?: ""
                            onFrame(WebSocketFrame.Close(code, message))
                        }

                        else -> {
                            // ignore
                        }
                    }
                }
            }
        }
    }

    actual suspend fun sendTextMessage(message: String) {
        wsSession?.send(message) ?: Log.e(tag, "WebSocket send text message without active session")
    }

    actual suspend fun disconnect() {
        wsSession?.close()
        wsSession = null
    }
}
