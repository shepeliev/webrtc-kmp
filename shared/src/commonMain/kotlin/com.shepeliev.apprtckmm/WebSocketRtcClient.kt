package com.shepeliev.apprtckmm

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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Message(
    val type: String,
    val sdp: String?,
    val id: String?,
    val label: Int?,
    val candidate: String?
)

@Serializable
class IceServerItem(
    val urls: String,
    val credential: String? = null
)

@Serializable
class TurnServerItem(
    val urls: List<String>,
    val username: String? = null,
    val credential: String? = null
)

@Serializable
class IceServersResponse(
    val iceServers: List<TurnServerItem>
)

@Serializable
class PcConfig(
    val rtcpMuxPolicy: String,
    val bundlePolicy: String,
    val iceServers: List<IceServerItem>
)

@Serializable
class RoomParams(
    @SerialName("room_id") val roomId: String? = null,
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("wss_url") val wssUrl: String? = null,
    @SerialName("wss_post_url") val wssPostUrl: String? = null,
    @SerialName("is_initiator") val isInitiator: Boolean? = null,
    @SerialName("ice_server_url") val iceServerUrl: String? = null,
    val messages: List<Message>,
    val pcConfig: PcConfig? = null,
    val result: String? = null
)

@Serializable
class RoomResponse(
    val result: String,
    val params: RoomParams
)

open class AppRtcClientException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)

class RoomFullException : AppRtcClientException()

private fun defaultHttpClient(): HttpClient = HttpClient {
    install(JsonFeature) {
        accept(ContentType.Text.Html)
        serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

class WebSocketRtcClient(
    private val events: AppRtcClient.SignalingEvents,
    private val httpClient: HttpClient = defaultHttpClient()
) : AppRtcClient {

    override suspend fun connectToRoom(connectionParameters: RoomConnectionParameters): SignalingParameters {
        val params = fetchRoomParams(connectionParameters)

        val messages = params.isInitiator?.takeIf { it }?.let { emptyMap() }
            ?: params.messages.groupBy { it.type }

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

        return SignalingParameters(
            iceServers = iceServersFromPcConfig + iceServersFromUrl,
            initiator = params.isInitiator!!,
            clientId = params.clientId!!,
            wssUrl = params.wssUrl!!,
            wssPostUrl = params.wssPostUrl!!,
            offerSdp = offerSdp,
            iceCandidates = iceCandidates
        )
    }

    private suspend fun fetchRoomParams(connectionParams: RoomConnectionParameters): RoomParams {
        val urlParams = connectionParams.urlParameters?.let { "?$it" } ?: ""
        val url = "${connectionParams.roomUrl}/join/${connectionParams.roomId}$urlParams"
        val response = httpClient.post<HttpResponse>(url)
        if (response.status.value != 200) {
            throw AppRtcClientException("Non-200 response to URL: $url")
        }

        val roomResponse = try {
            response.receive<RoomResponse>()
        } catch (e: Throwable) {
            throw AppRtcClientException(
                "Parsing the response from URL: $url failed.\n${response.readText()}",
                e
            )
        }

        if (roomResponse.result != "SUCCESS") {
            throw AppRtcClientException("Room response error: ${roomResponse.result}")
        }

        val params = roomResponse.params
        if (params.result == "FULL") throw RoomFullException()

        return params
    }

    private suspend fun fetchIceServers(url: String): List<IceServer> {
        val resp = httpClient.post<HttpResponse>(url) {
            header("Referer", "https://appr.tc")
        }

        if (resp.status.value != 200) {
            throw AppRtcClientException("Non-200 response when requesting ICE servers from ${url}")
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
        TODO("not implemented")
    }

    override suspend fun sendAnswerSdp(sdp: SessionDescription) {
        TODO("not implemented")
    }

    override suspend fun sendLocalIceCandidate(candidate: IceCandidate) {
        TODO("not implemented")
    }

    override suspend fun sendLocalIceCandidateRemovals(candidates: List<IceCandidate>) {
        TODO("not implemented")
    }

    override suspend fun disconnectFromRoom() {
        TODO("not implemented")
    }
}
