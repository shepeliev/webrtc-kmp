package com.shepeliev.webrtckmp

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
import dev.onvoid.webrtc.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual class PeerConnection actual constructor(rtcConfiguration: RtcConfiguration) {

    val platform = WebRtc.peerConnectionFactory.createPeerConnection(
        rtcConfiguration.android,
        AndroidPeerConnectionObserver()
    ) ?: error("Creating PeerConnection failed")

    actual val localDescription: SessionDescription?
        get() = platform.localDescription?.asCommon()

    actual val remoteDescription: SessionDescription?
        get() = platform.remoteDescription?.asCommon()

    actual val signalingState: SignalingState
        get() = platform.signalingState().asCommon()

    actual val iceConnectionState: IceConnectionState
        get() = platform.iceConnectionState().asCommon()

    actual val connectionState: PeerConnectionState
        get() = platform.connectionState().asCommon()

    actual val iceGatheringState: IceGatheringState
        get() = platform.iceGatheringState().asCommon()

    private val _peerConnectionEvent =
        MutableSharedFlow<PeerConnectionEvent>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    internal actual val peerConnectionEvent: Flow<PeerConnectionEvent> = _peerConnectionEvent.asSharedFlow()

    private val localTracks = mutableMapOf<String, MediaStreamTrack>()
    private val remoteTracks = mutableMapOf<String, MediaStreamTrack>()

    actual fun createDataChannel(
        label: String,
        id: Int,
        ordered: Boolean,
        maxRetransmitTimeMs: Int,
        maxRetransmits: Int,
        protocol: String,
        negotiated: Boolean
    ): DataChannel? {
        val init = AndroidDataChannel.Init().also {
            it.id = id
            it.ordered = ordered
            it.maxRetransmitTimeMs = maxRetransmitTimeMs
            it.maxRetransmits = maxRetransmits
            it.protocol = protocol
            it.negotiated = negotiated
        }
        return platform.createDataChannel(label, init)?.let { DataChannel(it) }
    }

    actual suspend fun createOffer(options: OfferAnswerOptions): SessionDescription {
        return suspendCoroutine { cont ->
            platform.createOffer(createSdpObserver(cont), options.toMediaConstraints())
        }
    }

    actual suspend fun createAnswer(options: OfferAnswerOptions): SessionDescription {
        return suspendCoroutine { cont ->
            platform.createAnswer(createSdpObserver(cont), options.toMediaConstraints())
        }
    }

    private fun OfferAnswerOptions.toMediaConstraints(): MediaConstraints {
        return MediaConstraints().apply {
            iceRestart?.let { mandatory += MediaConstraints.KeyValuePair("IceRestart", "$it") }
            offerToReceiveAudio?.let {
                mandatory += MediaConstraints.KeyValuePair("OfferToReceiveAudio", "$it")
            }
            offerToReceiveVideo?.let {
                mandatory += MediaConstraints.KeyValuePair("OfferToReceiveVideo", "$it")
            }
            voiceActivityDetection?.let {
                mandatory += MediaConstraints.KeyValuePair("VoiceActivityDetection", "$it")
            }
        }
    }

    private fun createSdpObserver(continuation: Continuation<SessionDescription>): SdpObserver {
        return object : SdpObserver {
            override fun onCreateSuccess(description: AndroidSessionDescription) {
                continuation.resume(description.asCommon())
            }

            override fun onSetSuccess() {
                // not applicable for creating SDP
            }

            override fun onCreateFailure(error: String?) {
                continuation.resumeWithException(RuntimeException("Creating SDP failed: $error"))
            }

            override fun onSetFailure(error: String?) {
                // not applicable for creating SDP
            }
        }
    }

    actual suspend fun setLocalDescription(description: SessionDescription) {
        return suspendCoroutine {
            platform.setLocalDescription(setSdpObserver(it), description.asPlatform())
        }
    }

    actual suspend fun setRemoteDescription(description: SessionDescription) {
        return suspendCoroutine {
            platform.setRemoteDescription(setSdpObserver(it), description.asPlatform())
        }
    }

    private fun setSdpObserver(continuation: Continuation<Unit>): SdpObserver {
        return object : SdpObserver {
            override fun onCreateSuccess(description: AndroidSessionDescription) {
                // not applicable for setting SDP
            }

            override fun onSetSuccess() {
                continuation.resume(Unit)
            }

            override fun onCreateFailure(error: String?) {
                // not applicable for setting SDP
            }

            override fun onSetFailure(error: String?) {
                continuation.resumeWithException(RuntimeException("Setting SDP failed: $error"))
            }
        }
    }

    actual fun setConfiguration(configuration: RtcConfiguration): Boolean {
        return platform.setConfiguration(configuration.android)
    }

    actual fun addIceCandidate(candidate: IceCandidate): Boolean {
        return platform.addIceCandidate(candidate.native)
    }

    actual fun removeIceCandidates(candidates: List<IceCandidate>): Boolean {
        return platform.removeIceCandidates(candidates.map { it.native }.toTypedArray())
    }

    actual fun getSenders(): List<RtpSender> = platform.senders.map {
        RtpSender(it, localTracks[it.track()?.id()])
    }

    actual fun getReceivers(): List<RtpReceiver> = platform.receivers.map {
        RtpReceiver(it, remoteTracks[it.track()?.id()])
    }

    actual fun getTransceivers(): List<RtpTransceiver> =
        platform.transceivers.map {
            val senderTrack = localTracks[it.sender.track()?.id()]
            val receiverTrack = remoteTracks[it.receiver.track()?.id()]
            RtpTransceiver(it, senderTrack, receiverTrack)
        }

    actual fun addTrack(track: MediaStreamTrack, vararg streams: MediaStream): RtpSender {
        val streamIds = streams.map { it.id }
        localTracks[track.id] = track
        return RtpSender(platform.addTrack(track.jvm, streamIds), track)
    }

    actual fun removeTrack(sender: RtpSender): Boolean {
        localTracks.remove(sender.track?.id)
        return platform.removeTrack(sender.native)
    }

    actual suspend fun getStats(): RtcStatsReport? {
        return suspendCoroutine { cont ->
            platform.getStats { cont.resume(RtcStatsReport(it)) }
        }
    }

    actual fun close() {
        remoteTracks.values.forEach(MediaStreamTrack::stop)
        remoteTracks.clear()
        platform.dispose()
    }

    internal inner class AndroidPeerConnectionObserver : PeerConnectionObserver {
        override fun onSignalingChange(newState: RTCSignalingState) {
            _peerConnectionEvent.tryEmit(SignalingStateChange(newState.asCommon()))
        }

        override fun onIceConnectionChange(newState: RTCIceConnectionState) {
            _peerConnectionEvent.tryEmit(IceConnectionStateChange(newState.asCommon()))
        }

        override fun onStandardizedIceConnectionChange(newState: RTCIceConnectionState) {
            _peerConnectionEvent.tryEmit(StandardizedIceConnectionChange(newState.asCommon()))
        }

        override fun onConnectionChange(newState: RTCPeerConnectionState) {
            _peerConnectionEvent.tryEmit(ConnectionStateChange(newState.asCommon()))
        }

        override fun onIceConnectionReceivingChange(receiving: Boolean) {}

        override fun onIceGatheringChange(newState: RTCIceGatheringState) {
            _peerConnectionEvent.tryEmit(IceGatheringStateChange(newState.asCommon()))
        }

        override fun onIceCandidate(candidate: RTCIceCandidate) {
            _peerConnectionEvent.tryEmit(NewIceCandidate(IceCandidate(candidate)))
        }

        override fun onIceCandidatesRemoved(candidates: Array<out RTCIceCandidate>) {
            _peerConnectionEvent.tryEmit(RemovedIceCandidates(candidates.map { IceCandidate(it) }))
        }

        override fun onAddStream(nativeStream: dev.onvoid.webrtc.media.MediaStream) {
            // this deprecated API should not longer be used
            // https://developer.mozilla.org/en-US/docs/Web/API/RTCPeerConnection/onaddstream
        }

        override fun onRemoveStream(nativeStream: dev.onvoid.webrtc.media.MediaStream) {
            // The removestream event has been removed from the WebRTC specification in favor of
            // the existing removetrack event on the remote MediaStream and the corresponding
            // MediaStream.onremovetrack event handler property of the remote MediaStream.
            // The RTCPeerConnection API is now track-based, so having zero tracks in the remote
            // stream is equivalent to the remote stream being removed and the old removestream event.
            // https://developer.mozilla.org/en-US/docs/Web/API/RTCPeerConnection/onremovestream
        }

        override fun onDataChannel(dataChannel: RTCDataChannel) {
            _peerConnectionEvent.tryEmit(NewDataChannel(DataChannel(dataChannel)))
        }

        override fun onRenegotiationNeeded() {
            _peerConnectionEvent.tryEmit(NegotiationNeeded)
        }

        override fun onAddTrack(
            receiver: RTCRtpReceiver?,
            mediaStreams: Array<out dev.onvoid.webrtc.media.MediaStream>?
        ) {
            val transceiver = platform.transceivers.find { it.receiver.id() == receiver.id() } ?: return

            val audioTracks = mediaStreams?.toList()?.flatMap { it.audioTracks.asIterable() }
                ?.map { remoteTracks.getOrPut(it.id) { AudioStreamTrack(it) } }

            val videoTracks = mediaStreams?.toList()?.flatMap { it.videoTracks.asIterable() }
                ?.map { remoteTracks.getOrPut(it.id) { VideoStreamTrack(it) } }

            val streams = mediaStreams?.map { stream ->
                MediaStream(
                    jvm = stream,
                    id = stream.id,
                ).also { stream ->
                    audioTracks?.forEach(stream::addTrack)
                    videoTracks?.forEach(stream::addTrack)
                }
            }

            val senderTrack = localTracks[transceiver.sender.track?.id]
            val receiverTrack = remoteTracks[receiver?.track?.id]

            val trackEvent = streams?.let {
                TrackEvent(
                    receiver = RtpReceiver(receiver, receiverTrack),
                    streams = it,
                    track = receiverTrack,
                    transceiver = RtpTransceiver(transceiver, senderTrack, receiverTrack)
                )
            }

            trackEvent?.let { Track(it) }?.let { _peerConnectionEvent.tryEmit(it) }
        }

        override fun onRemoveTrack(receiver: RTCRtpReceiver) {
            val track = remoteTracks.remove(receiver.track?.id)
            _peerConnectionEvent.tryEmit(RemoveTrack(RtpReceiver(receiver, track)))
            track?.stop()
        }
    }
}

private fun RTCSignalingState.asCommon(): SignalingState {
    return when (this) {
        RTCSignalingState.STABLE -> SignalingState.Stable
        RTCSignalingState.HAVE_LOCAL_OFFER -> SignalingState.HaveLocalOffer
        RTCSignalingState.HAVE_LOCAL_PR_ANSWER -> SignalingState.HaveLocalPranswer
        RTCSignalingState.HAVE_REMOTE_OFFER -> SignalingState.HaveRemoteOffer
        RTCSignalingState.HAVE_REMOTE_PR_ANSWER -> SignalingState.HaveRemotePranswer
        RTCSignalingState.CLOSED -> SignalingState.Closed
    }
}

private fun RTCIceConnectionState.asCommon(): IceConnectionState {
    return when (this) {
        RTCIceConnectionState.NEW -> IceConnectionState.New
        RTCIceConnectionState.CHECKING -> IceConnectionState.Checking
        RTCIceConnectionState.CONNECTED -> IceConnectionState.Connected
        RTCIceConnectionState.COMPLETED -> IceConnectionState.Completed
        RTCIceConnectionState.FAILED -> IceConnectionState.Failed
        RTCIceConnectionState.DISCONNECTED -> IceConnectionState.Disconnected
        RTCIceConnectionState.CLOSED -> IceConnectionState.Closed
    }
}

private fun RTCPeerConnectionState.asCommon(): PeerConnectionState {
    return when (this) {
        RTCPeerConnectionState.NEW -> PeerConnectionState.New
        RTCPeerConnectionState.CONNECTING -> PeerConnectionState.Connecting
        RTCPeerConnectionState.CONNECTED -> PeerConnectionState.Connected
        RTCPeerConnectionState.DISCONNECTED -> PeerConnectionState.Disconnected
        RTCPeerConnectionState.FAILED -> PeerConnectionState.Failed
        RTCPeerConnectionState.CLOSED -> PeerConnectionState.Closed
    }
}

private fun RTCIceGatheringState.asCommon(): IceGatheringState {
    return when (this) {
        RTCIceGatheringState.NEW -> IceGatheringState.New
        RTCIceGatheringState.GATHERING -> IceGatheringState.Gathering
        RTCIceGatheringState.COMPLETE -> IceGatheringState.Complete
    }
}
