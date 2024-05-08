package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.RTCPeerConnection
import com.shepeliev.webrtckmp.externals.addIceCandidate
import com.shepeliev.webrtckmp.externals.createAnswer
import com.shepeliev.webrtckmp.externals.createDataChannel
import com.shepeliev.webrtckmp.externals.createOffer
import com.shepeliev.webrtckmp.externals.getReceivers
import com.shepeliev.webrtckmp.externals.getSenders
import com.shepeliev.webrtckmp.externals.getStats
import com.shepeliev.webrtckmp.externals.getTransceivers
import com.shepeliev.webrtckmp.externals.setLocalDescription
import com.shepeliev.webrtckmp.externals.setRemoteDescription
import com.shepeliev.webrtckmp.externals.streams
import com.shepeliev.webrtckmp.externals.toSessionDescription
import com.shepeliev.webrtckmp.internal.AudioTrackImpl
import com.shepeliev.webrtckmp.internal.Console
import com.shepeliev.webrtckmp.internal.VideoTrackImpl
import com.shepeliev.webrtckmp.internal.toPlatform
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

actual class PeerConnection actual constructor(rtcConfiguration: RtcConfiguration) {
    actual val localDescription: SessionDescription? get() = platform.localDescription?.toSessionDescription()
    actual val remoteDescription: SessionDescription? get() = platform.remoteDescription?.toSessionDescription()
    actual val signalingState: SignalingState get() = platform.signalingState.toSignalingState()
    actual val iceConnectionState: IceConnectionState get() = platform.iceConnectionState.toIceConnectionState()
    actual val connectionState: PeerConnectionState get() = platform.connectionState.toPeerConnectionState()
    actual val iceGatheringState: IceGatheringState get() = platform.iceGatheringState.toIceGatheringState()

    private val platform: RTCPeerConnection

    private val _peerConnectionEvent =
        MutableSharedFlow<PeerConnectionEvent>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    internal actual val peerConnectionEvent: Flow<PeerConnectionEvent> = _peerConnectionEvent.asSharedFlow()

    private val scope = MainScope()

    init {
        platform = RTCPeerConnection(rtcConfiguration).apply {
            onsignalingstatechange = {
                scope.launch {
                    val event = PeerConnectionEvent.SignalingStateChange(this@PeerConnection.signalingState)
                    _peerConnectionEvent.emit(event)
                }
            }
            oniceconnectionstatechange = {
                scope.launch {
                    val event = PeerConnectionEvent.IceConnectionStateChange(this@PeerConnection.iceConnectionState)
                    _peerConnectionEvent.emit(event)
                }
            }
            onconnectionstatechange = {
                scope.launch {
                    val event = PeerConnectionEvent.ConnectionStateChange(this@PeerConnection.connectionState)
                    _peerConnectionEvent.emit(event)
                }
            }
            onicegatheringstatechange = {
                scope.launch {
                    val event = PeerConnectionEvent.IceGatheringStateChange(this@PeerConnection.iceGatheringState)
                    _peerConnectionEvent.tryEmit(event)
                }
            }
            onicecandidate = { iceEvent ->
                scope.launch {
                    val event = iceEvent.candidate?.let { PeerConnectionEvent.NewIceCandidate(IceCandidate(it)) }
                    event?.let { _peerConnectionEvent.emit(it) }
                }
            }
            ondatachannel = { dataChannelEvent ->
                scope.launch {
                    val event = PeerConnectionEvent.NewDataChannel(DataChannel(dataChannelEvent.channel))
                    _peerConnectionEvent.emit(event)
                }
            }
            onnegotiationneeded = {
                scope.launch {
                    _peerConnectionEvent.emit(PeerConnectionEvent.NegotiationNeeded)
                }
            }
            ontrack = { rtcTrackEvent ->
                scope.launch {
                    val trackEvent = TrackEvent(
                        receiver = RtpReceiver(rtcTrackEvent.receiver),
                        streams = rtcTrackEvent.streams.map { MediaStream(it) },
                        track = rtcTrackEvent.track.let {
                            when (it.kind) {
                                "audio" -> AudioTrackImpl(it)
                                "video" -> VideoTrackImpl(it)
                                else -> throw IllegalArgumentException("Unsupported track kind: ${it.kind}")
                            }
                        },
                        transceiver = RtpTransceiver(rtcTrackEvent.transceiver)
                    )
                    _peerConnectionEvent.emit(PeerConnectionEvent.Track(trackEvent))
                }
            }
        }
    }

    actual fun createDataChannel(
        label: String,
        id: Int,
        ordered: Boolean,
        maxPacketLifeTimeMs: Int,
        maxRetransmits: Int,
        protocol: String,
        negotiated: Boolean
    ): DataChannel? {
        return platform.createDataChannel(
            label,
            id,
            ordered,
            maxPacketLifeTimeMs,
            maxRetransmits,
            protocol,
            negotiated
        )?.let { return DataChannel(it) }
    }

    actual suspend fun createOffer(options: OfferAnswerOptions): SessionDescription {
        return platform.createOffer(options).toSessionDescription()
    }

    actual suspend fun createAnswer(options: OfferAnswerOptions): SessionDescription {
        return platform.createAnswer(options).toSessionDescription()
    }

    actual suspend fun setLocalDescription(description: SessionDescription) {
        platform.setLocalDescription(description)
    }

    actual suspend fun setRemoteDescription(description: SessionDescription) {
        platform.setRemoteDescription(description)
    }

    actual fun setConfiguration(configuration: RtcConfiguration): Boolean {
        return runCatching { platform.setConfiguration(configuration.toPlatform()) }
            .onFailure { Console.error("Set RTCConfiguration failed: $it") }
            .map { true }
            .getOrDefault(false)
    }

    actual suspend fun addIceCandidate(candidate: IceCandidate): Boolean {
        return runCatching { platform.addIceCandidate(candidate) }
            .onFailure { Console.error("Add ICE candidate failed: $it") }
            .map { true }
            .getOrDefault(false)
    }

    actual fun removeIceCandidates(candidates: List<IceCandidate>): Boolean {
        Console.warn("removeIceCandidates is not supported in JS")
        return true
    }

    actual fun getSenders(): List<RtpSender> {
        return platform.getSenders().map { RtpSender(it) }
    }

    actual fun getReceivers(): List<RtpReceiver> {
        return platform.getReceivers().map { RtpReceiver(it) }
    }

    actual fun getTransceivers(): List<RtpTransceiver> {
        return platform.getTransceivers().map { RtpTransceiver(it) }
    }

    actual fun addTrack(track: MediaStreamTrack, vararg streams: MediaStream): RtpSender {
        require(track is MediaStreamTrackImpl)
        val platformSender = platform.addTrack(track.platform, *Array(streams.size) { streams[it].js })
        return RtpSender(platformSender)
    }

    actual fun removeTrack(sender: RtpSender): Boolean {
        return runCatching { platform.removeTrack(sender.js) }
            .onFailure { Console.error("Remove track failed: $it") }
            .map { true }
            .getOrDefault(false)
    }

    actual suspend fun getStats(): RtcStatsReport? {
        return runCatching { platform.getStats() }
            .map { RtcStatsReport() }
            .onFailure { Console.error("Get stats failed: $it") }
            .getOrNull()
    }

    actual fun close() {
        platform.close()
        scope.launch {
            _peerConnectionEvent.emit(PeerConnectionEvent.SignalingStateChange(SignalingState.Closed))
            scope.cancel()
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
