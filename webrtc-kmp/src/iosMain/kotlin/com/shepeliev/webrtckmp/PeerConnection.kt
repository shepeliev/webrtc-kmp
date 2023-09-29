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
import WebRTC.RTCRtpReceiver
import WebRTC.RTCRtpSender
import WebRTC.RTCRtpTransceiver
import WebRTC.RTCRtpTransceiverInit
import WebRTC.RTCSessionDescription
import WebRTC.RTCSignalingState
import WebRTC.RTCVideoTrack
import WebRTC.dataChannelForLabel
import WebRTC.kRTCMediaStreamTrackKindAudio
import WebRTC.kRTCMediaStreamTrackKindVideo
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

    val native: RTCPeerConnection = checkNotNull(
        WebRtc.peerConnectionFactory.peerConnectionWithConfiguration(
            configuration = rtcConfiguration.native,
            constraints = RTCMediaConstraints(),
            delegate = this
        )
    ) { "Failed to create peer connection" }

    actual val localDescription: SessionDescription? get() = native.localDescription?.asCommon()

    actual val remoteDescription: SessionDescription? get() = native.remoteDescription?.asCommon()

    actual val signalingState: SignalingState
        get() = rtcSignalingStateAsCommon(native.signalingState())

    actual val iceConnectionState: IceConnectionState
        get() = rtcIceConnectionStateAsCommon(native.iceConnectionState())

    actual val connectionState: PeerConnectionState
        get() = rtcPeerConnectionStateAsCommon(native.connectionState())

    actual val iceGatheringState: IceGatheringState
        get() = rtcIceGatheringStateAsCommon(native.iceGatheringState())

    private val _peerConnectionEvent =
        MutableSharedFlow<PeerConnectionEvent>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    internal actual val peerConnectionEvent: Flow<PeerConnectionEvent> = _peerConnectionEvent.asSharedFlow()

    private val localTracks = mutableMapOf<String, MediaStreamTrackImpl>()
    private val remoteTracks = mutableMapOf<String, MediaStreamTrackImpl>()

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
        return native.dataChannelForLabel(label, config)?.let { DataChannel(it) }
    }

    actual suspend fun createOffer(options: OfferAnswerOptions): SessionDescription {
        val constraints = options.toRTCMediaConstraints()
        val sessionDescription: RTCSessionDescription = native.awaitResult {
            offerForConstraints(constraints, it)
        }
        return sessionDescription.asCommon()
    }

    actual suspend fun createAnswer(options: OfferAnswerOptions): SessionDescription {
        val constraints = options.toRTCMediaConstraints()
        val sessionDescription: RTCSessionDescription = native.awaitResult {
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
        native.await { setLocalDescription(description.asIos(), it) }
    }

    actual suspend fun setRemoteDescription(description: SessionDescription) {
        native.await { setRemoteDescription(description.asIos(), it) }
    }

    actual fun setConfiguration(configuration: RtcConfiguration): Boolean {
        return native.setConfiguration(configuration.native)
    }

    actual fun addIceCandidate(candidate: IceCandidate): Boolean {
        native.addIceCandidate(candidate.native)
        return true
    }

    actual fun removeIceCandidates(candidates: List<IceCandidate>): Boolean {
        native.removeIceCandidates(candidates.map { it.native })
        return true
    }

    actual fun getSenders(): List<RtpSender> = native.senders.map {
        val iosSender = it as RTCRtpSender
        RtpSender(iosSender, localTracks[iosSender.track?.trackId])
    }

    actual fun getReceivers(): List<RtpReceiver> = native.receivers.map {
        val iosReceiver = it as RTCRtpReceiver
        RtpReceiver(iosReceiver, remoteTracks[iosReceiver.track?.trackId])
    }

    actual fun getTransceivers(): List<RtpTransceiver> = native.transceivers.map {
        val iosTransceiver = it as RTCRtpTransceiver
        val senderTrack = localTracks[iosTransceiver.sender.track?.trackId]
        val receiverTrack = remoteTracks[iosTransceiver.receiver.track?.trackId]
        RtpTransceiver(iosTransceiver, senderTrack, receiverTrack)
    }

    actual fun addTrack(track: MediaStreamTrack, vararg streams: MediaStream): RtpSender {
        require(track is MediaStreamTrackImpl)

        val streamIds = streams.map { it.id }
        val iosSender = checkNotNull(native.addTrack(track.native, streamIds)) { "Failed to add track" }
        localTracks[track.id] = track
        return RtpSender(iosSender, track)
    }

    actual fun addTransceiver(
        track: MediaStreamTrack,
        direction: RtpTransceiverDirection,
        streamIds: List<String>,
        sendEncodings: List<RtpEncodingParameters>,
    ): RtpTransceiver {
        require(track is MediaStreamTrackImpl)

        val init = RTCRtpTransceiverInit().apply {
            this.direction = direction.asNative()
            this.streamIds = streamIds
            this.sendEncodings = sendEncodings.map { it.native }
        }
        val nativeTransceiver = native.addTransceiverWithTrack(track.native, init) ?: error("Failed to add transceiver")
        val nativeReceiverTrack = nativeTransceiver.receiver.track()
        val receiverTrack = when (nativeReceiverTrack?.kind()) {
            kRTCMediaStreamTrackKindVideo -> RemoteVideoTrack(nativeReceiverTrack as RTCVideoTrack)
            kRTCMediaStreamTrackKindAudio -> RemoteAudioTrack(nativeReceiverTrack as RTCAudioTrack)
            else -> null
        }

        return RtpTransceiver(nativeTransceiver, track, receiverTrack)
    }

    actual fun removeTrack(sender: RtpSender): Boolean {
        localTracks.remove(sender.track?.id)
        return native.removeTrack(sender.ios)
    }

    actual suspend fun getStats(): RtcStatsReport? {
        // TODO not implemented yet
        return null
    }

    actual suspend fun getStats(sender: RtpSender): RtcStatsReport? {
        // TODO not implemented yet
        return null
    }

    actual suspend fun getStats(receiver: RtpReceiver): RtcStatsReport? {
        // TODO not implemented yet
        return null
    }

    actual fun close() {
        remoteTracks.values.forEach(MediaStreamTrack::stop)
        remoteTracks.clear()
        native.close()
    }

    override fun peerConnection(peerConnection: RTCPeerConnection, didChangeSignalingState: RTCSignalingState) {
        val event = SignalingStateChange(rtcSignalingStateAsCommon(didChangeSignalingState))
        _peerConnectionEvent.tryEmit(event)
    }

    @Suppress("CONFLICTING_OVERLOADS", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun peerConnection(peerConnection: RTCPeerConnection, didAddStream: RTCMediaStream) {
        // this deprecated API should not longer be used
        // https://developer.mozilla.org/en-US/docs/Web/API/RTCPeerConnection/onaddstream
    }

    @Suppress("CONFLICTING_OVERLOADS", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun peerConnection(peerConnection: RTCPeerConnection, didRemoveStream: RTCMediaStream) {
        // The removestream event has been removed from the WebRTC specification in favor of
        // the existing removetrack event on the remote MediaStream and the corresponding
        // MediaStream.onremovetrack event handler property of the remote MediaStream.
        // The RTCPeerConnection API is now track-based, so having zero tracks in the remote
        // stream is equivalent to the remote stream being removed and the old removestream event.
        // https://developer.mozilla.org/en-US/docs/Web/API/RTCPeerConnection/onremovestream
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

    override fun peerConnection(peerConnection: RTCPeerConnection, didAddReceiver: RTCRtpReceiver, streams: List<*>) {
        val transceiver = native.transceivers
            .map { it as RTCRtpTransceiver }
            .find { it.receiver.receiverId == didAddReceiver.receiverId }
            ?: return

        val iosStreams = streams.map { it as RTCMediaStream }

        val audioTracks = iosStreams
            .flatMap { it.audioTracks }
            .map { it as RTCAudioTrack }
            .map { remoteTracks.getOrPut(it.trackId) { RemoteAudioTrack(it) } }

        val videoTracks = iosStreams
            .flatMap { it.videoTracks }
            .map { it as RTCVideoTrack }
            .map { remoteTracks.getOrPut(it.trackId) { RemoteVideoTrack(it) } }

        val commonStreams = iosStreams.map { iosStream ->
            MediaStream(ios = iosStream, id = iosStream.streamId).also { stream ->
                audioTracks.forEach(stream::addTrack)
                videoTracks.forEach(stream::addTrack)
            }
        }

        val receiverTrack = remoteTracks[didAddReceiver.track?.trackId]
        val senderTrack = localTracks[transceiver.sender.track?.trackId]

        val trackEvent = TrackEvent(
            receiver = RtpReceiver(didAddReceiver, receiverTrack),
            streams = commonStreams,
            track = receiverTrack,
            transceiver = RtpTransceiver(transceiver, senderTrack, receiverTrack)
        )

        val event = Track(trackEvent)
        _peerConnectionEvent.tryEmit(event)
    }

    override fun peerConnection(peerConnection: RTCPeerConnection, didRemoveReceiver: RTCRtpReceiver) {
        val track = remoteTracks.remove(didRemoveReceiver.track?.trackId)
        val event = RemoveTrack(RtpReceiver(didRemoveReceiver, track))
        _peerConnectionEvent.tryEmit(event)
        track?.stop()
    }
}
