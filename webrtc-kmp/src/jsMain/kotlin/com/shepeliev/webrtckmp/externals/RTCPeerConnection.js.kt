package com.shepeliev.webrtckmp.externals

import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.OfferAnswerOptions
import com.shepeliev.webrtckmp.RtcConfiguration
import com.shepeliev.webrtckmp.SessionDescription
import com.shepeliev.webrtckmp.internal.toPlatform
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.json

internal actual fun RTCPeerConnection(configuration: RtcConfiguration): RTCPeerConnection {
    return JsRTCPeerConnection(configuration.toPlatform())
}

internal actual suspend fun RTCPeerConnection.createOffer(options: OfferAnswerOptions): RTCSessionDescription {
    return (this as JsRTCPeerConnection).createOffer(options.toPlatform()).await()
}

internal actual suspend fun RTCPeerConnection.createAnswer(options: OfferAnswerOptions): RTCSessionDescription {
    return (this as JsRTCPeerConnection).createAnswer(options.toPlatform()).await()
}

internal actual suspend fun RTCPeerConnection.setLocalDescription(description: SessionDescription) {
    return (this as JsRTCPeerConnection).setLocalDescription(description.toPlatform()).await()
}

internal actual suspend fun RTCPeerConnection.setRemoteDescription(description: SessionDescription) {
    return (this as JsRTCPeerConnection).setRemoteDescription(description.toPlatform()).await()
}

internal actual suspend fun RTCPeerConnection.addIceCandidate(candidate: IceCandidate) {
    return (this as JsRTCPeerConnection).addIceCandidate(candidate.js).await()
}

internal actual fun RTCPeerConnection.getReceivers(): List<RTCRtpReceiver> {
    return (this as JsRTCPeerConnection).getReceivers().toList()
}

internal actual fun RTCPeerConnection.getSenders(): List<RTCRtpSender> {
    return (this as JsRTCPeerConnection).getSenders().toList()
}

internal actual fun RTCPeerConnection.getTransceivers(): List<RTCRtpTransceiver> {
    return (this as JsRTCPeerConnection).getTransceivers().toList()
}

internal actual suspend fun RTCPeerConnection.getStats(): RTCStatsReport {
    return (this as JsRTCPeerConnection).getStats().await()
}

internal actual fun RTCPeerConnection.createDataChannel(
    label: String,
    id: Int,
    ordered: Boolean,
    maxPacketLifeTimeMs: Int,
    maxRetransmits: Int,
    protocol: String,
    negotiated: Boolean
): RTCDataChannel? {
    val options = json().apply {
        if (id > -1) add(json("id" to id))
        if (maxPacketLifeTimeMs > -1) add(json("maxRetransmitTimeMs" to maxPacketLifeTimeMs))
        if (maxRetransmits > -1) add(json("maxRetransmits" to maxRetransmits))
        if (protocol.isNotEmpty()) add(json("protocol" to protocol))
        add(json("ordered" to ordered, "negotiated" to negotiated))
    }
    return (this as JsRTCPeerConnection).createDataChannel(label, options)
}

@JsName("RTCPeerConnection")
internal external class JsRTCPeerConnection(configuration: dynamic) : RTCPeerConnection {
    override val localDescription: RTCSessionDescription?
    override val remoteDescription: RTCSessionDescription?
    override val signalingState: String
    override val iceConnectionState: String
    override val connectionState: String
    override val iceGatheringState: String
    override var onsignalingstatechange: (() -> Unit)?
    override var oniceconnectionstatechange: (() -> Unit)?
    override var onconnectionstatechange: (() -> Unit)?
    override var onicegatheringstatechange: (() -> Unit)?
    override var onicecandidate: ((RTCPeerConnectionIceEvent) -> Unit)?
    override var ondatachannel: ((RTCDataChannelEvent) -> Unit)?
    override var onnegotiationneeded: (() -> Unit)?
    override var ontrack: ((RTCTrackEvent) -> Unit)?

    override fun close()
    override fun addTrack(track: PlatformMediaStreamTrack, vararg streams: PlatformMediaStream): RTCRtpSender
    override fun setConfiguration(configuration: RTCPeerConnectionConfiguration)
    override fun removeTrack(sender: RTCRtpSender)

    fun createDataChannel(label: String, options: dynamic): RTCDataChannel
    fun createOffer(options: dynamic): Promise<RTCSessionDescription>
    fun createAnswer(options: dynamic): Promise<RTCSessionDescription>
    fun addIceCandidate(candidate: RTCIceCandidate): Promise<Unit>
    fun getReceivers(): Array<RTCRtpReceiver>
    fun getSenders(): Array<RTCRtpSender>
    fun getStats(): Promise<RTCStatsReport>
    fun getTransceivers(): Array<RTCRtpTransceiver>
    fun setLocalDescription(sdp: dynamic): Promise<Unit>
    fun setRemoteDescription(sdp: dynamic): Promise<Unit>

    companion object {
        suspend fun generateCertificate(options: dynamic): Promise<RTCCertificate>
    }
}
