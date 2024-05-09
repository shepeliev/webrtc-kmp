package com.shepeliev.webrtckmp.externals

import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.OfferAnswerOptions
import com.shepeliev.webrtckmp.RtcConfiguration
import com.shepeliev.webrtckmp.SessionDescription
import com.shepeliev.webrtckmp.internal.WasmRtcConfiguration
import com.shepeliev.webrtckmp.internal.await
import com.shepeliev.webrtckmp.internal.toList
import com.shepeliev.webrtckmp.internal.toWasmJs
import kotlin.js.Promise

internal actual fun RTCPeerConnection(configuration: RtcConfiguration): RTCPeerConnection =
    WasmRTCPeerConnection(configuration.toWasmJs())

internal actual suspend fun RTCPeerConnection.createOffer(options: OfferAnswerOptions): RTCSessionDescription {
    return (this as WasmRTCPeerConnection).createOffer(options.toWasmJs()).await()
}

internal actual suspend fun RTCPeerConnection.createAnswer(options: OfferAnswerOptions): RTCSessionDescription {
    return (this as WasmRTCPeerConnection).createAnswer(options.toWasmJs()).await()
}

internal actual suspend fun RTCPeerConnection.setLocalDescription(description: SessionDescription) {
    return (this as WasmRTCPeerConnection).setLocalDescription(description.toWasmJs()).await()
}

internal actual suspend fun RTCPeerConnection.setRemoteDescription(description: SessionDescription) {
    return (this as WasmRTCPeerConnection).setRemoteDescription(description.toWasmJs()).await()
}

internal actual suspend fun RTCPeerConnection.addIceCandidate(candidate: IceCandidate) {
    return (this as WasmRTCPeerConnection).addIceCandidate(candidate.js).await()
}

internal actual fun RTCPeerConnection.getReceivers(): List<RTCRtpReceiver> {
    return (this as WasmRTCPeerConnection).getReceivers().toList().filterNotNull()
}

internal actual fun RTCPeerConnection.getSenders(): List<RTCRtpSender> {
    return (this as WasmRTCPeerConnection).getSenders().toList().filterNotNull()
}

internal actual fun RTCPeerConnection.getTransceivers(): List<RTCRtpTransceiver> {
    return (this as WasmRTCPeerConnection).getTransceivers().toList().filterNotNull()
}

internal actual suspend fun RTCPeerConnection.getStats(): RTCStatsReport {
    return (this as WasmRTCPeerConnection).getStats().await()
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
    val options = when {
        id > -1 && maxPacketLifeTimeMs > -1 -> createRTCDataChannelOptionsWithMaxPacketLifeTime(
            id,
            ordered,
            maxPacketLifeTimeMs,
            protocol,
            negotiated
        )

        id > -1 && maxRetransmits > -1 -> createRTCDataChannelOptionsWithMaxRetransmits(
            id,
            ordered,
            maxRetransmits,
            protocol,
            negotiated
        )

        maxPacketLifeTimeMs > -1 -> createRTCDataChannelOptionsWithMaxPacketLifeTime(
            ordered,
            maxPacketLifeTimeMs,
            protocol,
            negotiated
        )

        maxRetransmits > -1 -> createRTCDataChannelOptionsWithMaxRetransmits(
            ordered,
            maxRetransmits,
            protocol,
            negotiated
        )

        else -> createRTCDataChannelOptions(ordered, protocol, negotiated)
    }

    return (this as WasmRTCPeerConnection).createDataChannel(label, options)
}

@JsName("RTCPeerConnection")
private external class WasmRTCPeerConnection(configuration: WasmRtcConfiguration) : RTCPeerConnection, JsAny {
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

    fun createDataChannel(label: String, options: RTCDataChannelOptions): RTCDataChannel?
    fun createOffer(options: JsAny?): Promise<WasmRTCSessionDescription>
    fun createAnswer(options: JsAny?): Promise<WasmRTCSessionDescription>
    fun addIceCandidate(candidate: RTCIceCandidate): Promise<JsAny?>
    fun getReceivers(): JsArray<WasmRTCRtpReceiver>
    fun getSenders(): JsArray<WasmRTCRtpSender>
    fun getStats(): Promise<WasmRTCStatsReport>
    fun getTransceivers(): JsArray<WasmRTCRtpTransceiver>
    fun setLocalDescription(sdp: JsAny): Promise<JsAny>
    fun setRemoteDescription(sdp: JsAny): Promise<JsAny>
}

@Suppress("UNUSED_PARAMETER")
private fun createRTCDataChannelOptions(
    id: Int,
    ordered: Boolean,
    protocol: String,
    negotiated: Boolean
): RTCDataChannelOptions = js(
    """({
        id: id,
        ordered: ordered,
        protocol: protocol,
        negotiated: negotiated
    })"""
)

@Suppress("UNUSED_PARAMETER")
private fun createRTCDataChannelOptionsWithMaxRetransmits(
    id: Int,
    ordered: Boolean,
    maxRetransmits: Int,
    protocol: String,
    negotiated: Boolean
): RTCDataChannelOptions = js(
    """({
        id: id,
        ordered: ordered,
        maxRetransmits: maxRetransmits,
        protocol: protocol,
        negotiated: negotiated
    })"""
)

@Suppress("UNUSED_PARAMETER")
private fun createRTCDataChannelOptionsWithMaxPacketLifeTime(
    id: Int,
    ordered: Boolean,
    maxPacketLifeTime: Int,
    protocol: String,
    negotiated: Boolean
): RTCDataChannelOptions = js(
    """({
        id: id,
        ordered: ordered,
        maxPacketLifeTime: maxPacketLifeTime,
        protocol: protocol,
        negotiated: negotiated
    })"""
)

@Suppress("UNUSED_PARAMETER")
private fun createRTCDataChannelOptions(
    ordered: Boolean,
    protocol: String,
    negotiated: Boolean
): RTCDataChannelOptions = js(
    """({
        ordered: ordered,
        protocol: protocol,
        negotiated: negotiated
    })"""
)

@Suppress("UNUSED_PARAMETER")
private fun createRTCDataChannelOptionsWithMaxRetransmits(
    ordered: Boolean,
    maxRetransmits: Int,
    protocol: String,
    negotiated: Boolean
): RTCDataChannelOptions = js(
    """({
        ordered: ordered,
        maxRetransmits: maxRetransmits,
        protocol: protocol,
        negotiated: negotiated
    })"""
)

@Suppress("UNUSED_PARAMETER")
private fun createRTCDataChannelOptionsWithMaxPacketLifeTime(
    ordered: Boolean,
    maxPacketLifeTime: Int,
    protocol: String,
    negotiated: Boolean
): RTCDataChannelOptions = js(
    """({
        ordered: ordered,
        maxPacketLifeTime: maxPacketLifeTime,
        protocol: protocol,
        negotiated: negotiated
    })"""
)
