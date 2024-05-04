package com.shepeliev.webrtckmp.externals

import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.OfferAnswerOptions
import com.shepeliev.webrtckmp.RtcConfiguration
import com.shepeliev.webrtckmp.SessionDescription

internal external interface RTCPeerConnection {
    val localDescription: RTCSessionDescription?
    val remoteDescription: RTCSessionDescription?
    val signalingState: String
    val iceConnectionState: String
    val connectionState: String
    val iceGatheringState: String

    var onsignalingstatechange: (() -> Unit)?
    var oniceconnectionstatechange: (() -> Unit)?
    var onconnectionstatechange: (() -> Unit)?
    var onicegatheringstatechange: (() -> Unit)?
    var onicecandidate: ((RTCPeerConnectionIceEvent) -> Unit)?
    var ondatachannel: ((RTCDataChannelEvent) -> Unit)?
    var onnegotiationneeded: (() -> Unit)?
    var ontrack: ((RTCTrackEvent) -> Unit)?

    fun close()
    fun addTrack(track: PlatformMediaStreamTrack, vararg streams: PlatformMediaStream): RTCRtpSender
    fun setConfiguration(configuration: RTCPeerConnectionConfiguration)
    fun removeTrack(sender: RTCRtpSender)
}

internal expect fun RTCPeerConnection(configuration: RtcConfiguration): RTCPeerConnection
internal expect suspend fun RTCPeerConnection.createOffer(options: OfferAnswerOptions): RTCSessionDescription
internal expect suspend fun RTCPeerConnection.createAnswer(options: OfferAnswerOptions): RTCSessionDescription
internal expect suspend fun RTCPeerConnection.setLocalDescription(description: SessionDescription)
internal expect suspend fun RTCPeerConnection.setRemoteDescription(description: SessionDescription)
internal expect suspend fun RTCPeerConnection.addIceCandidate(candidate: IceCandidate)
internal expect fun RTCPeerConnection.getReceivers(): List<RTCRtpReceiver>
internal expect fun RTCPeerConnection.getSenders(): List<RTCRtpSender>
internal expect fun RTCPeerConnection.getTransceivers(): List<RTCRtpTransceiver>
internal expect suspend fun RTCPeerConnection.getStats(): RTCStatsReport
internal expect fun RTCPeerConnection.createDataChannel(
    label: String,
    id: Int,
    ordered: Boolean,
    maxPacketLifeTimeMs: Int,
    maxRetransmits: Int,
    protocol: String,
    negotiated: Boolean,
): RTCDataChannel?
