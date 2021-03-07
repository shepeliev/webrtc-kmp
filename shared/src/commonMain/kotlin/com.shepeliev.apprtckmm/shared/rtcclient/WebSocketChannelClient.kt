package com.shepeliev.apprtckmm.shared.rtcclient

import com.shepeliev.apprtckmm.shared.Log
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WebSocketChannelClient(private val events: WebSocketChannelEvents) {
    var state: WebSocketConnectionState = WebSocketConnectionState.New
        private set

    private val tag = "WSChannelRTCClient"
    private lateinit var ws: WebSocketConnection
    private lateinit var wsServerUrl: String
    private lateinit var postServerUrl: String
    private var roomId: String? = null
    private var clientId: String? = null
    private var closeEvent: Boolean = false
    private val wsSendQueue = mutableListOf<Message>()

    suspend fun connect(wsUrl: String, postUrl: String) {
        if (state != WebSocketConnectionState.New) {
            Log.e(tag, "WebSocket is already connected.")
            return
        }

        wsServerUrl = wsUrl
        postServerUrl = postUrl
        closeEvent = false

        ws = WebSocketConnection()

        Log.d(tag, "Connecting to WSS: $wsUrl")
        ws.connect(wsServerUrl) {
            when (it) {
                is WebSocketFrame.Text -> {
                    Log.d(tag, "WSS->C: ${it.message}")
                    if (state in arrayOf(
                            WebSocketConnectionState.Connected,
                            WebSocketConnectionState.Registered
                        )
                    ) {
                        events.onWebSocketMessage(it.message)
                    }
                }

                is WebSocketFrame.Close -> {
                    Log.d(
                        tag,
                        "WebSocket connection closed. Code: ${it.code}. Reason: ${it.message}. State: $state"
                    )
                    if (state != WebSocketConnectionState.Closed) {
                        state = WebSocketConnectionState.Closed
                        events.onWebSocketClose()
                    }
                }
            }
        }

        state = WebSocketConnectionState.Connected
    }

    suspend fun register(roomId: String, clientId: String) {
        this.roomId = roomId
        this.clientId = clientId
        if (state != WebSocketConnectionState.Connected) {
            Log.w(tag, "WebSocket register() in state $state")
            return
        }
        Log.d(tag, "Registering WebSocket for room $roomId. ClientID: $clientId")
        val wsMessage = Json.encodeToString(
            WebSocketMessage(
                cmd = "register",
                roomId = roomId,
                clientId = clientId
            )
        )
        sendJson(wsMessage)
        state = WebSocketConnectionState.Registered
        Log.d(tag, "Registered WebSocket for room $roomId. ClientID: $clientId")
        wsSendQueue.forEach { send(it) }
        wsSendQueue.clear()
    }

    suspend fun send(message: Message) {
        when (state) {
            WebSocketConnectionState.New,
            WebSocketConnectionState.Connected -> {
                // Store outgoing messages and send them after websocket client
                // is registered.
                Log.d(tag, "WS -> ACC: $message")
                wsSendQueue += message
            }
            WebSocketConnectionState.Error,
            WebSocketConnectionState.Closed -> {
                Log.e(tag, "WebSocket send() in error or closed state: $state")
            }
            WebSocketConnectionState.Registered -> {
                val jsonMessage = Json.encodeToString(message)
                val wsMessage =
                    Json.encodeToString(WebSocketMessage(cmd = "send", msg = jsonMessage))
                sendJson(wsMessage)
            }
        }
    }

    private suspend fun sendJson(json: String) {
        Log.d(tag, "C->WSS: $json")
        ws.sendTextMessage(json)
    }

    suspend fun disconnect() {
        Log.d(tag, "Disconnect WebSocket. State: $state")
        if (state == WebSocketConnectionState.Registered) {
            sendJson("""{"cmd":"send", "msg":"{\"type\":\"bye\"}"}""")
            state = WebSocketConnectionState.Connected
            sendWssMessage(HttpMethod.Delete)
        }

        if (state in arrayOf(WebSocketConnectionState.Connected, WebSocketConnectionState.Error)) {
            ws.disconnect()
            state = WebSocketConnectionState.Closed
        }
        Log.d(tag, "Disconnecting WebSocket done.")
    }

    // Asynchronously send POST/DELETE to WebSocket server.
    private suspend fun sendWssMessage(method: HttpMethod, message: String? = null) {
        val url = "$postServerUrl/$roomId/$clientId"
        Log.d(tag, "WS ${method.value} : $url : $message")
        val client = HttpClient()

        val response = try {
            client.request<HttpResponse>(url) {
                this.method = method
                message?.let { body = message }
            }
        } catch (e: Throwable) {
            reportError("WS ${method.value} error: ${e.message}")
            return
        }

        if (response.status.value != 200) {
            reportError("WS ${method.value} error (status = ${response.status.value}): ${response.readText()}")
        }
    }

    private fun reportError(errorMessage: String) {
        Log.e(tag, errorMessage)
        if (state != WebSocketConnectionState.Error) {
            state = WebSocketConnectionState.Error
            events.onWebSocketError(errorMessage)
        }
    }
}

@Serializable
class WebSocketMessage(
    val cmd: String,
    val msg: String? = null,
    @SerialName("roomid") val roomId: String? = null,
    @SerialName("clientid") val clientId: String? = null,
)

/**
 * Callback interface for messages delivered on WebSocket.
 * All events are dispatched from a looper executor thread.
 */
interface WebSocketChannelEvents {
    fun onWebSocketMessage(message: String)
    fun onWebSocketClose()
    fun onWebSocketError(description: String)
}

/**
 * Possible WebSocket connection states.
 */
enum class WebSocketConnectionState {
    New, Connected, Registered, Closed, Error
}
