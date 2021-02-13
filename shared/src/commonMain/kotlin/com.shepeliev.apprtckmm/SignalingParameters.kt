package com.shepeliev.apprtckmm

import com.shepeliev.webrtckmm.IceCandidate
import com.shepeliev.webrtckmm.IceServer
import com.shepeliev.webrtckmm.PeerConnection
import com.shepeliev.webrtckmm.SessionDescription
import kotlinx.serialization.Serializable

/**
 * Struct holding the signaling parameters of an AppRTC room.
 */
data class SignalingParameters(
    val iceServers: List<IceServer>,
    val initiator: Boolean,
    val clientId: String,
    val wssUrl: String,
    val wssPostUrl: String,
    val offerSdp: SessionDescription?,
    val iceCandidates: List<IceCandidate>
)
