package com.shepeliev.apprtckmm.shared.rtcclient

import com.shepeliev.apprtckmm.shared.Log
import com.shepeliev.apprtckmm.shared.RoomConnectionParameters
import com.shepeliev.apprtckmm.shared.SignalingParameters
import com.shepeliev.webrtckmm.IceCandidate
import com.shepeliev.webrtckmm.IceServer
import com.shepeliev.webrtckmm.SessionDescription
import com.shepeliev.webrtckmm.SessionDescriptionType
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WebSocketRtcClient(
    private val events: AppRtcClient.SignalingEvents,
    private val httpClient: HttpClient = defaultHttpClient()
) : AppRtcClient, WebSocketChannelEvents {

    private val tag = "WSRTCClient"
    private val wsClient = WebSocketChannelClient(this)
    private var roomState = ConnectionState.New
    private var isInitiator: Boolean = false
    private lateinit var messageUrl: String
    private lateinit var leaveUrl: String

    override suspend fun connectToRoom(connectionParameters: RoomConnectionParameters): SignalingParameters {
        val urlParams = getQueryString(connectionParameters)
        val url = "${connectionParameters.roomUrl}/join/${connectionParameters.roomId}$urlParams"

        Log.d(tag, "Connect to room: $url")

        val params = fetchRoomParams(url)

        val messages = params.isInitiator?.takeIf { it }?.let { emptyMap() }
            ?: params.messages
                .map { Json.decodeFromString<Message>(it) }
                .groupBy { it.type }

        val offerSdp = messages["offer"]?.firstOrNull()?.let {
            if (it.sdp.isNullOrBlank()) throw AppRtcClientException("Offer SDP is malformed")
            SessionDescription(SessionDescriptionType.Offer, it.sdp)
        }

        val iceCandidates = messages["candidate"]
            ?.filter {
                !it.id.isNullOrBlank() && it.label != null && !it.candidate.isNullOrBlank()
            }
            ?.map {
                IceCandidate(sdpMid = it.id!!, sdpMLineIndex = it.label!!, sdp = it.candidate!!)
            } ?: emptyList()

        val iceServersFromPcConfig = params.pcConfig?.iceServers?.map {
            IceServer(listOf(it.urls), password = it.credential ?: "")
        } ?: emptyList()

        val isTurnPresent = params.pcConfig?.iceServers?.any { it.urls.startsWith("turn:") }
            ?: false

        val iceServersFromUrl = if (!isTurnPresent && !params.iceServerUrl.isNullOrBlank()) {
            fetchIceServers(params.iceServerUrl)
        } else emptyList()

        val signalingParams = SignalingParameters(
            iceServers = iceServersFromPcConfig + iceServersFromUrl,
            initiator = params.isInitiator!!,
            clientId = params.clientId!!,
            wssUrl = params.wssUrl!!,
            wssPostUrl = params.wssPostUrl!!,
            offerSdp = offerSdp,
            iceCandidates = iceCandidates
        )

        isInitiator = signalingParams.initiator
        messageUrl =
            "${connectionParameters.roomUrl}/message/${connectionParameters.roomId}/${signalingParams.clientId}${
                getQueryString(connectionParameters)
            }"
        leaveUrl =
            "${connectionParameters.roomUrl}/leave/${connectionParameters.roomId}/${signalingParams.clientId}${
                getQueryString(connectionParameters)
            }"
        roomState = ConnectionState.Connected

        wsClient.connect(signalingParams.wssUrl, signalingParams.wssPostUrl)
        wsClient.register(connectionParameters.roomId, signalingParams.clientId)

        return signalingParams
    }

    private suspend fun fetchRoomParams(url: String): RoomParams {
        val response = httpClient.post<HttpResponse>(url)
        if (response.status.value != 200) {
            throw AppRtcClientException("Non-200 response to URL: $url")
        }

        val roomResponse = try {
            response.receive<RoomResponse>()
        } catch (e: Throwable) {
            val responseText = response.readText()
            Log.w(tag, responseText)
            throw AppRtcClientException("Parsing the response from URL: $url failed.", e)
        }

        if (roomResponse.result == "FULL") throw RoomFullException()
        if (roomResponse.result != "SUCCESS") {
            throw AppRtcClientException("Room response error: ${roomResponse.result}")
        }

        return roomResponse.params
    }

    private fun getQueryString(connectionParams: RoomConnectionParameters): String {
        return connectionParams.urlParameters?.let { "?$it" } ?: ""
    }

    private suspend fun fetchIceServers(url: String): List<IceServer> {
        val resp = httpClient.post<HttpResponse>(url) {
            header("Referer", "https://appr.tc")
        }

        if (resp.status.value != 200) {
            throw AppRtcClientException("Non-200 response when requesting ICE servers from $url")
        }

        val iceServersResponse = try {
            resp.receive<IceServersResponse>()
        } catch (e: Throwable) {
            throw AppRtcClientException("Parsing the ICE servers response failed.", e)
        }

        return iceServersResponse.iceServers.map {
            IceServer(it.urls, it.username ?: "", it.credential ?: "")
        }
    }

    override suspend fun sendOfferSdp(sdp: SessionDescription) {
        if (roomState != ConnectionState.Connected) {
            reportError("Sending offer SDP in non connected state.")
            return
        }

        val message = Message(type = "offer", sdp = sdp.description)
        sendPostMessage(MessageType.Message, messageUrl, message)
    }

    override suspend fun sendAnswerSdp(sdp: SessionDescription) {
        val message = Message(type = "answer", sdp = sdp.description)
        wsClient.send(message)
    }

    override suspend fun sendLocalIceCandidate(candidate: IceCandidate) {
        val message = Message(
            type = "candidate",
            label = candidate.sdpMLineIndex,
            id = candidate.sdpMid,
            candidate = candidate.sdp
        )
        if (isInitiator) {
            if (roomState != ConnectionState.Connected) {
                reportError("Sending ICE candidate in non connected state.")
                return
            }
            sendPostMessage(MessageType.Message, messageUrl, message)
        } else {
            wsClient.send(message)
        }
    }

    override suspend fun sendLocalIceCandidateRemovals(candidates: List<IceCandidate>) {
        val message = Message(
            type = "remove-candidates",
            candidates = candidates.map {
                Message.Candidate(id = it.sdpMid, label = it.sdpMLineIndex, candidate = it.sdp)
            }
        )
        if (isInitiator) {
            if (roomState != ConnectionState.Connected) {
                reportError("Sending ICE candidate removals in non connected state.")
                return
            }
            sendPostMessage(MessageType.Message, messageUrl, message)
        }
    }

    override suspend fun disconnectFromRoom() {
        Log.d(tag, "Disconnect. Room state: $roomState")
        roomState.also {
            roomState = ConnectionState.Closed
            if (it == ConnectionState.Connected) {
                Log.d(tag, "Closing room")
                sendPostMessage(MessageType.Leave, leaveUrl)
            }
        }
        wsClient.disconnect()
    }

    private suspend fun sendPostMessage(
        messageType: MessageType,
        url: String,
        message: Message? = null
    ) {
        val json = message?.let { Json.encodeToString(message) }
        Log.d(tag, "C->GAE: $url ${json?.let { ". Message: $it" } ?: ""}")
        val response = try {
            httpClient.post<HttpResponse>(url) {
                json?.let { body = json }
            }
        } catch (e: Throwable) {
            reportError("GAE POST error: ${e.message}")
            return
        }

        if (response.status.value != 200) {
            reportError("GAE POST error (status = ${response.status.value}): ${response.readText()}")
        }

        if (messageType != MessageType.Message) {
            return
        }

        val messageResponse = try {
            response.receive<MessageResponse>()
        } catch (e: Throwable) {
            reportError("GAE POST JSON error: ${e.message}, response:\n\n ${response.readText()}")
            return
        }

        if (messageResponse.result != "SUCCESS") {
            reportError("GAE POST error: ${messageResponse.result}")
        }
    }

    private fun reportError(error: String) {
        if (roomState != ConnectionState.Error) {
            roomState = ConnectionState.Error
            events.onChannelError(error)
        }
    }

    override fun onWebSocketMessage(message: String) {
        if (wsClient.state != WebSocketConnectionState.Registered) {
            Log.e(tag, "Got WebSocket message in non registered state.")
            return
        }

        try {
            val wsMessage = Json.decodeFromString<IncomingWsMessage>(message)
            if (wsMessage.msg.isNotEmpty()) {
                val msg = Json.decodeFromString<Message>(wsMessage.msg)
                when (msg.type) {
                    "candidate" -> {
                        events.onRemoteIceCandidate(
                            IceCandidate(
                                sdpMid = msg.id!!,
                                sdpMLineIndex = msg.label!!,
                                sdp = msg.candidate!!
                            )
                        )
                    }

                    "remove-candidates" -> {
                        val candidates = msg.candidates!!.map {
                            IceCandidate(
                                sdpMid = it.id!!,
                                sdpMLineIndex = it.label!!,
                                sdp = it.candidate!!
                            )
                        }
                        events.onRemoteIceCandidatesRemoved(candidates)
                    }

                    "answer" -> {
                        if (isInitiator) {
                            val sdp = SessionDescription(SessionDescriptionType.Answer, msg.sdp!!)
                            events.onRemoteDescription(sdp)
                        } else {
                            reportError("Received answer for call initiator: $message")
                        }
                    }

                    "offer" -> {
                        if (!isInitiator) {
                            val sdp = SessionDescription(SessionDescriptionType.Offer, msg.sdp!!)
                            events.onRemoteDescription(sdp)
                        } else {
                            reportError("Received answer for call receiver: $message")
                        }
                    }

                    "bye" -> {
                        events.onChannelClose()
                    }

                    else -> {
                        reportError("Unexpected WebSocket message: $message")
                    }
                }
            } else {
                wsMessage.error?.let { reportError("WebSocket error message: $it") }
                    ?: reportError("Unexpected WebSocket message: $message")
            }
        } catch (e: Throwable) {
            reportError("WebSocket message JSON parsing error: ${e.message}")
            return
        }
    }

    override fun onWebSocketClose() {
        events.onChannelClose()
    }

    override fun onWebSocketError(description: String) {
        reportError("WebSocket error: $description")
    }

    private enum class ConnectionState { New, Connected, Closed, Error }

    private enum class MessageType { Message, Leave }
}

private fun defaultHttpClient() = HttpClient {
    install(JsonFeature) {
        accept(ContentType.Text.Html)
        serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}
