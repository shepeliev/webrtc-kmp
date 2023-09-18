package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.PeerConnectionEvent.ConnectionStateChange
import com.shepeliev.webrtckmp.PeerConnectionEvent.IceConnectionStateChange
import com.shepeliev.webrtckmp.PeerConnectionEvent.IceGatheringStateChange
import com.shepeliev.webrtckmp.PeerConnectionEvent.NegotiationNeeded
import com.shepeliev.webrtckmp.PeerConnectionEvent.NewDataChannel
import com.shepeliev.webrtckmp.PeerConnectionEvent.NewIceCandidate
import com.shepeliev.webrtckmp.PeerConnectionEvent.SignalingStateChange
import com.shepeliev.webrtckmp.PeerConnectionEvent.Track
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.js.Json
import kotlin.js.json

actual class PeerConnection actual constructor(rtcConfiguration: RtcConfiguration) {

    val native: RTCPeerConnection

    actual val localDescription: SessionDescription? get() = native.localDescription?.asCommon()

    actual val remoteDescription: SessionDescription? get() = native.remoteDescription?.asCommon()

    actual val signalingState: SignalingState
        get() = native.signalingState.toSignalingState()

    actual val iceConnectionState: IceConnectionState
        get() = native.iceConnectionState.toIceConnectionState()

    actual val connectionState: PeerConnectionState
        get() = native.connectionState.toPeerConnectionState()

    actual val iceGatheringState: IceGatheringState
        get() = native.iceGatheringState.toIceGatheringState()

    private val _peerConnectionEvent =
        MutableSharedFlow<PeerConnectionEvent>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    internal actual val peerConnectionEvent: Flow<PeerConnectionEvent> = _peerConnectionEvent.asSharedFlow()

    init {
        WebRtcAdapter

        native = RTCPeerConnection(rtcConfiguration.js).apply {
            onsignalingstatechange = {
                _peerConnectionEvent.tryEmit(SignalingStateChange(this@PeerConnection.signalingState))
            }
            oniceconnectionstatechange = {
                _peerConnectionEvent.tryEmit(IceConnectionStateChange(this@PeerConnection.iceConnectionState))
            }
            onconnectionstatechange = {
                _peerConnectionEvent.tryEmit(ConnectionStateChange(this@PeerConnection.connectionState))
            }
            onicegatheringstatechange = {
                _peerConnectionEvent.tryEmit(IceGatheringStateChange(this@PeerConnection.iceGatheringState))
            }
            onicecandidate = { iceEvent ->
                iceEvent.candidate?.let { _peerConnectionEvent.tryEmit(NewIceCandidate(IceCandidate(it))) }
            }
            ondatachannel = { dataChannelEvent ->
                _peerConnectionEvent.tryEmit(NewDataChannel(DataChannel(dataChannelEvent.channel)))
            }
            onnegotiationneeded = { _peerConnectionEvent.tryEmit(NegotiationNeeded) }
            ontrack = { rtcTrackEvent ->
                val trackEvent = TrackEvent(
                    receiver = RtpReceiver(rtcTrackEvent.receiver),
                    streams = rtcTrackEvent.streams.map { MediaStream(it) },
                    track = rtcTrackEvent.track.asCommon(),
                    transceiver = RtpTransceiver(rtcTrackEvent.transceiver)
                )
                _peerConnectionEvent.tryEmit(Track(trackEvent))
            }
        }
    }

    actual fun createDataChannel(
        label: String,
        id: Int,
        ordered: Boolean,
        maxRetransmitTimeMs: Int,
        maxRetransmits: Int,
        protocol: String,
        negotiated: Boolean,
    ): DataChannel? {
        val options = json().apply {
            if (id > -1) add(json("id" to id))
            if (maxRetransmitTimeMs > -1) add(json("maxRetransmitTimeMs" to maxRetransmitTimeMs))
            if (maxRetransmits > -1) add(json("maxRetransmits" to maxRetransmits))
            if (protocol.isNotEmpty()) add(json("protocol" to protocol))
            add(
                json(
                    "ordered" to ordered,
                    "negotiated" to negotiated
                )
            )
        }
        return native.createDataChannel(label, options)?.let { DataChannel(it) }
    }

    actual suspend fun createOffer(options: OfferAnswerOptions): SessionDescription {
        val sessionDescription = native.createOffer(options.toJson()).await()
        return sessionDescription.asCommon()
    }

    actual suspend fun createAnswer(options: OfferAnswerOptions): SessionDescription {
        val sessionDescription = native.createAnswer(options.toJson()).await()
        return sessionDescription.asCommon()
    }

    actual suspend fun setLocalDescription(description: SessionDescription) {
        native.setLocalDescription(description.asJs()).await()
    }

    actual suspend fun setRemoteDescription(description: SessionDescription) {
        native.setRemoteDescription(description.asJs()).await()
    }

    actual fun setConfiguration(configuration: RtcConfiguration): Boolean {
        native.setConfiguration(configuration.js)
        return true
    }

    actual fun addIceCandidate(candidate: IceCandidate): Boolean {
        native.addIceCandidate(candidate.js)
        return true
    }

    actual fun removeIceCandidates(candidates: List<IceCandidate>): Boolean {
        // not implemented for JS target
        return true
    }

    actual fun getSenders(): List<RtpSender> = native.getSenders().map { RtpSender(it) }

    actual fun getReceivers(): List<RtpReceiver> = native.getReceivers().map { RtpReceiver(it) }

    actual fun getTransceivers(): List<RtpTransceiver> {
        return native.getTransceivers().map { RtpTransceiver(it) }
    }

    actual fun addTrack(track: MediaStreamTrack, vararg streams: MediaStream): RtpSender {
        require(track is MediaStreamTrackImpl)

        val jsStreams = streams.map { it.js }.toTypedArray()
        return RtpSender(native.addTrack(track.native, *jsStreams))
    }

    actual fun addTransceiver(
        track: MediaStreamTrack,
        direction: RtpTransceiverDirection,
        streamIds: List<String>,
        sendEncodings: List<RtpEncodingParameters>,
    ): RtpTransceiver {
        require(track is MediaStreamTrackImpl)

        val jsStreamIds = streamIds.toTypedArray()
        val jsSendEncodings = sendEncodings.map { it.toJson() }.toTypedArray()
        return RtpTransceiver(
            native.addTransceiver(
                track.native,
                json(
                    "direction" to direction.toCanonicalForm(),
                    "streams" to jsStreamIds,
                    "sendEncodings" to jsSendEncodings
                )
            )
        )
    }

    actual fun removeTrack(sender: RtpSender): Boolean {
        native.removeTrack(sender.native)
        return true
    }

    actual suspend fun getStats(): RtcStatsReport? {
        // TODO implement
        return null
    }

    actual suspend fun getStats(sender: RtpSender): RtcStatsReport? {
        // TODO implement
        return null
    }

    actual suspend fun getStats(receiver: RtpReceiver): RtcStatsReport? {
        // TODO implement
        return null
    }

    actual fun close() {
        native.close()
    }

    private fun OfferAnswerOptions.toJson(): Json {
        return json().apply {
            iceRestart?.also { add(json("iceRestart" to it)) }
            offerToReceiveAudio?.also { add(json("offerToReceiveAudio" to it)) }
            offerToReceiveVideo?.also { add(json("offerToReceiveVideo" to it)) }
            voiceActivityDetection?.also { add(json("voiceActivityDetection" to it)) }
        }
    }

    private fun String.toSignalingState(): SignalingState = when (this) {
        "stable" -> SignalingState.Stable
        "have-local-offer" -> SignalingState.HaveLocalOffer
        "have-remote-offer" -> SignalingState.HaveRemoteOffer
        "have-local-pranswer" -> SignalingState.HaveLocalPranswer
        "have-remote-pranswer" -> SignalingState.HaveRemotePranswer
        "closed" -> SignalingState.Closed
        else -> throw IllegalArgumentException("Illegal signaling state: $this")
    }

    private fun String.toIceConnectionState(): IceConnectionState = when (this) {
        "new" -> IceConnectionState.New
        "checking" -> IceConnectionState.Checking
        "connected" -> IceConnectionState.Connected
        "completed" -> IceConnectionState.Completed
        "failed" -> IceConnectionState.Failed
        "disconnected" -> IceConnectionState.Disconnected
        "closed" -> IceConnectionState.Closed
        else -> throw IllegalArgumentException("Illegal ICE connection state: $this")
    }

    private fun String.toPeerConnectionState(): PeerConnectionState = when (this) {
        "new" -> PeerConnectionState.New
        "connecting" -> PeerConnectionState.Connecting
        "connected" -> PeerConnectionState.Connected
        "disconnected" -> PeerConnectionState.Disconnected
        "failed" -> PeerConnectionState.Failed
        "closed" -> PeerConnectionState.Closed
        else -> throw IllegalArgumentException("Illegal connection state: $this")
    }

    private fun String.toIceGatheringState(): IceGatheringState = when (this) {
        "new" -> IceGatheringState.New
        "gathering" -> IceGatheringState.Gathering
        "complete" -> IceGatheringState.Complete
        else -> throw IllegalArgumentException("Illegal ICE gathering state: $this")
    }
}
