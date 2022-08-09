package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack
import WebRTC.RTCDataChannel
import WebRTC.RTCDataChannelConfiguration
import WebRTC.RTCIceCandidate
import WebRTC.RTCIceConnectionState
import WebRTC.RTCIceGatheringState
import WebRTC.RTCMediaConstraints
import WebRTC.RTCMediaStream
import WebRTC.RTCPeerConnection
import WebRTC.RTCPeerConnectionDelegateProtocol
import WebRTC.RTCPeerConnectionState
import WebRTC.RTCRtpMediaType
import WebRTC.RTCRtpReceiver
import WebRTC.RTCRtpSender
import WebRTC.RTCRtpTransceiver
import WebRTC.RTCSessionDescription
import WebRTC.RTCSignalingState
import WebRTC.RTCVideoTrack
import WebRTC.dataChannelForLabel
import com.shepeliev.webrtckmp.PeerConnectionEvent.ConnectionStateChange
import com.shepeliev.webrtckmp.PeerConnectionEvent.IceConnectionStateChange
import com.shepeliev.webrtckmp.PeerConnectionEvent.IceGatheringStateChange
import com.shepeliev.webrtckmp.PeerConnectionEvent.NegotiationNeeded
import com.shepeliev.webrtckmp.PeerConnectionEvent.NewDataChannel
import com.shepeliev.webrtckmp.PeerConnectionEvent.NewIceCandidate
import com.shepeliev.webrtckmp.PeerConnectionEvent.RemoveTrack
import com.shepeliev.webrtckmp.PeerConnectionEvent.RemovedIceCandidates
import com.shepeliev.webrtckmp.PeerConnectionEvent.SignalingStateChange
import com.shepeliev.webrtckmp.PeerConnectionEvent.StandardizedIceConnectionChange
import com.shepeliev.webrtckmp.PeerConnectionEvent.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import platform.darwin.NSObject

actual class PeerConnection actual constructor(
    rtcConfiguration: RtcConfiguration
) : NSObject(), RTCPeerConnectionDelegateProtocol {

    val ios: RTCPeerConnection = checkNotNull(
        factory.peerConnectionWithConfiguration(
            configuration = rtcConfiguration.native,
            constraints = RTCMediaConstraints(),
            delegate = this
        )
    ) { "Failed to create peer connection" }

    actual val localDescription: SessionDescription? get() = ios.localDescription?.asCommon()

    actual val remoteDescription: SessionDescription? get() = ios.remoteDescription?.asCommon()

    actual val signalingState: SignalingState
        get() = rtcSignalingStateAsCommon(ios.signalingState())

    actual val iceConnectionState: IceConnectionState
        get() = rtcIceConnectionStateAsCommon(ios.iceConnectionState())

    actual val connectionState: PeerConnectionState
        get() = rtcPeerConnectionStateAsCommon(ios.connectionState())

    actual val iceGatheringState: IceGatheringState
        get() = rtcIceGatheringStateAsCommon(ios.iceGatheringState())

    private val _peerConnectionEvent =
        MutableSharedFlow<PeerConnectionEvent>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    internal actual val peerConnectionEvent: Flow<PeerConnectionEvent> = _peerConnectionEvent.asSharedFlow()

    actual fun createDataChannel(
        label: String,
        id: Int,
        ordered: Boolean,
        maxRetransmitTimeMs: Int,
        maxRetransmits: Int,
        protocol: String,
        negotiated: Boolean
    ): DataChannel? {
        val config = RTCDataChannelConfiguration().also {
            it.channelId = id
            it.isOrdered = ordered
            it.maxRetransmitTimeMs = maxRetransmitTimeMs.toLong()
            it.maxRetransmits = maxRetransmits
            it.protocol = protocol
            it.isNegotiated = negotiated
        }
        return ios.dataChannelForLabel(label, config)?.let { DataChannel(it) }
    }

    actual suspend fun createOffer(options: OfferAnswerOptions): SessionDescription {
        val constraints = options.toRTCMediaConstraints()
        val sessionDescription: RTCSessionDescription = ios.awaitResult {
            offerForConstraints(constraints, it)
        }
        return sessionDescription.asCommon()
    }

    actual suspend fun createAnswer(options: OfferAnswerOptions): SessionDescription {
        val constraints = options.toRTCMediaConstraints()
        val sessionDescription: RTCSessionDescription = ios.awaitResult {
            answerForConstraints(constraints, it)
        }
        return sessionDescription.asCommon()
    }

    private fun OfferAnswerOptions.toRTCMediaConstraints(): RTCMediaConstraints {
        val mandatory = mutableMapOf<Any?, String?>().apply {
            iceRestart?.let { this += "IceRestart" to "$it" }
            offerToReceiveAudio?.let { this += "OfferToReceiveAudio" to "$it" }
            offerToReceiveVideo?.let { this += "OfferToReceiveVideo" to "$it" }
            voiceActivityDetection?.let { this += "VoiceActivityDetection" to "$it" }
        }
        return RTCMediaConstraints(mandatory, null)
    }

    actual suspend fun setLocalDescription(description: SessionDescription) {
        ios.await { setLocalDescription(description.asIos(), it) }
    }

    actual suspend fun setRemoteDescription(description: SessionDescription) {
        ios.await { setRemoteDescription(description.asIos(), it) }
    }

    actual fun setConfiguration(configuration: RtcConfiguration): Boolean {
        return ios.setConfiguration(configuration.native)
    }

    actual fun addIceCandidate(candidate: IceCandidate): Boolean {
        ios.addIceCandidate(candidate.native)
        return true
    }

    actual fun removeIceCandidates(candidates: List<IceCandidate>): Boolean {
        ios.removeIceCandidates(candidates.map { it.native })
        return true
    }

    actual fun getSenders(): List<RtpSender> = ios.senders.map { RtpSender(it as RTCRtpSender) }

    actual fun getReceivers(): List<RtpReceiver> =
        ios.receivers.map { RtpReceiver(it as RTCRtpReceiver) }

    actual fun getTransceivers(): List<RtpTransceiver> =
        ios.transceivers.map { RtpTransceiver(it as RTCRtpTransceiver) }

    actual fun addTrack(track: MediaStreamTrack, vararg streams: MediaStream): RtpSender {
        val streamIds = streams.map { it.id }
        return RtpSender(ios.addTrack(track.ios, streamIds))
    }

    actual fun removeTrack(sender: RtpSender): Boolean = ios.removeTrack(sender.native)

    actual suspend fun getStats(): RtcStatsReport? {
        // TODO not implemented yet
        return null
    }

    actual fun close() {
        ios.close()
    }

    override fun peerConnection(peerConnection: RTCPeerConnection, didChangeSignalingState: RTCSignalingState) {
        val event = SignalingStateChange(rtcSignalingStateAsCommon(didChangeSignalingState))
        _peerConnectionEvent.tryEmit(event)
    }

    @Suppress("CONFLICTING_OVERLOADS", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun peerConnection(peerConnection: RTCPeerConnection, didAddStream: RTCMediaStream) {
        // TODO add emitting event
    }

    @Suppress("CONFLICTING_OVERLOADS", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun peerConnection(peerConnection: RTCPeerConnection, didRemoveStream: RTCMediaStream) {
        // TODO add emitting event
    }

    override fun peerConnectionShouldNegotiate(peerConnection: RTCPeerConnection) {
        _peerConnectionEvent.tryEmit(NegotiationNeeded)
    }

    @Suppress("CONFLICTING_OVERLOADS", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun peerConnection(peerConnection: RTCPeerConnection, didChangeIceConnectionState: RTCIceConnectionState) {
        val event = IceConnectionStateChange(rtcIceConnectionStateAsCommon(didChangeIceConnectionState))
        _peerConnectionEvent.tryEmit(event)
    }

    override fun peerConnection(peerConnection: RTCPeerConnection, didChangeIceGatheringState: RTCIceGatheringState) {
        val event = IceGatheringStateChange(rtcIceGatheringStateAsCommon(didChangeIceGatheringState))
        _peerConnectionEvent.tryEmit(event)
    }

    override fun peerConnection(peerConnection: RTCPeerConnection, didGenerateIceCandidate: RTCIceCandidate) {
        val event = NewIceCandidate(IceCandidate(didGenerateIceCandidate))
        _peerConnectionEvent.tryEmit(event)
    }

    override fun peerConnection(peerConnection: RTCPeerConnection, didRemoveIceCandidates: List<*>) {
        val candidates = didRemoveIceCandidates.map { IceCandidate(it as RTCIceCandidate) }
        val event = RemovedIceCandidates(candidates)
        _peerConnectionEvent.tryEmit(event)
    }

    override fun peerConnection(peerConnection: RTCPeerConnection, didOpenDataChannel: RTCDataChannel) {
        val event = NewDataChannel(DataChannel(didOpenDataChannel))
        _peerConnectionEvent.tryEmit(event)
    }

    @Suppress("CONFLICTING_OVERLOADS", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeStandardizedIceConnectionState: RTCIceConnectionState
    ) {
        val event = StandardizedIceConnectionChange(
            rtcIceConnectionStateAsCommon(didChangeStandardizedIceConnectionState)
        )
        _peerConnectionEvent.tryEmit(event)
    }

    override fun peerConnection(peerConnection: RTCPeerConnection, didChangeConnectionState: RTCPeerConnectionState) {
        val event = ConnectionStateChange(rtcPeerConnectionStateAsCommon(didChangeConnectionState))
        _peerConnectionEvent.tryEmit(event)
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didStartReceivingOnTransceiver: RTCRtpTransceiver
    ) {

        val sender = didStartReceivingOnTransceiver.sender
        val receiver = didStartReceivingOnTransceiver.receiver

        val track = when (didStartReceivingOnTransceiver.mediaType) {
            RTCRtpMediaType.RTCRtpMediaTypeAudio -> AudioStreamTrack(receiver.track as RTCAudioTrack)
            RTCRtpMediaType.RTCRtpMediaTypeVideo -> VideoStreamTrack(receiver.track as RTCVideoTrack)
            RTCRtpMediaType.RTCRtpMediaTypeData, RTCRtpMediaType.RTCRtpMediaTypeUnsupported -> null
            else -> error("Unknown RTCRtpMediaType: ${didStartReceivingOnTransceiver.mediaType}")
        }

        val tracks = track?.let { listOf(it) } ?: emptyList()

        val streams = sender.streamIds.takeIf { it.isNotEmpty() }
            ?.map { id -> MediaStream(ios = null, "$id", tracks) }
            ?: listOf(MediaStream(tracks))

        val trackEvent = TrackEvent(
            receiver = RtpReceiver(didStartReceivingOnTransceiver.receiver),
            streams = streams,
            track = track,
            transceiver = RtpTransceiver(didStartReceivingOnTransceiver)
        )

        val event = Track(trackEvent)
        _peerConnectionEvent.tryEmit(event)
    }

    override fun peerConnection(peerConnection: RTCPeerConnection, didRemoveReceiver: RTCRtpReceiver) {
        val event = RemoveTrack(RtpReceiver(didRemoveReceiver))
        _peerConnectionEvent.tryEmit(event)
    }
}
