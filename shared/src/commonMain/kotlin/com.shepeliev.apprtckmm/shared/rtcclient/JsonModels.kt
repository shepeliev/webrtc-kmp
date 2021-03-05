package com.shepeliev.apprtckmm.shared.rtcclient

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class IceServerItem(
    val urls: String,
    val credential: String? = null
)

@Serializable
class IceServersResponse(
    val iceServers: List<TurnServerItem>
)

@Serializable
class Message(
    val type: String,
    val sdp: String? = null,
    val id: String? = null,
    val label: Int? = null,
    val candidate: String? = null,
    val candidates: List<Candidate>? = null
) {

    @Serializable
    class Candidate(
        val id: String? = null,
        val label: Int? = null,
        val candidate: String? = null
    )
}

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
    val messages: List<String>,
    val pcConfig: PcConfig? = null,
    val result: String? = null
)

@Serializable
class RoomResponse(
    val result: String,
    val params: RoomParams
)

@Serializable
class TurnServerItem(
    val urls: List<String>,
    val username: String? = null,
    val credential: String? = null
)

@Serializable
class MessageResponse(val result: String)

@Serializable
class IncomingWsMessage(val msg: String, val error: String? = null)
