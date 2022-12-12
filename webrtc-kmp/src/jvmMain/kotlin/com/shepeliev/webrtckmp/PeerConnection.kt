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
import dev.onvoid.webrtc.CreateSessionDescriptionObserver
import dev.onvoid.webrtc.PeerConnectionObserver
import dev.onvoid.webrtc.RTCAnswerOptions
import dev.onvoid.webrtc.RTCDataChannel
import dev.onvoid.webrtc.RTCDataChannelInit
import dev.onvoid.webrtc.RTCIceCandidate
import dev.onvoid.webrtc.RTCIceConnectionState
import dev.onvoid.webrtc.RTCIceGatheringState
import dev.onvoid.webrtc.RTCOfferOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import dev.onvoid.webrtc.RTCPeerConnection
import dev.onvoid.webrtc.RTCPeerConnectionIceErrorEvent
import dev.onvoid.webrtc.RTCPeerConnectionState
import dev.onvoid.webrtc.RTCRtpReceiver
import dev.onvoid.webrtc.RTCRtpTransceiver
import dev.onvoid.webrtc.RTCRtpTransceiverDirection
import dev.onvoid.webrtc.RTCSessionDescription
import dev.onvoid.webrtc.RTCSignalingState
import dev.onvoid.webrtc.SetSessionDescriptionObserver
import dev.onvoid.webrtc.media.audio.AudioTrack
import dev.onvoid.webrtc.media.video.VideoTrack
import dev.onvoid.webrtc.media.MediaStream as NativeMediaStream

actual class PeerConnection actual constructor(rtcConfiguration: RtcConfiguration) {

    val native: RTCPeerConnection by lazy {
        WebRtc.peerConnectionFactory.createPeerConnection(
            rtcConfiguration.native,
            NativePeerConnectionObserver()
        ) ?: error("Creating PeerConnection failed")
    }

    actual val localDescription: SessionDescription?
        get() = native.localDescription?.asCommon()

    actual val remoteDescription: SessionDescription?
        get() = native.remoteDescription?.asCommon()

    actual val signalingState: SignalingState
        get() = native.signalingState.asCommon()

    actual val iceConnectionState: IceConnectionState
        get() = native.iceConnectionState.asCommon()

    actual val connectionState: PeerConnectionState
        get() = native.connectionState.asCommon()

    actual val iceGatheringState: IceGatheringState
        get() = native.iceGatheringState.asCommon()

    private val _peerConnectionEvent = MutableSharedFlow<PeerConnectionEvent>(
        extraBufferCapacity = FLOW_BUFFER_CAPACITY,
    )
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
        val init = RTCDataChannelInit().apply {
            this.id = id
            this.ordered = ordered
            this.maxRetransmits = maxRetransmits
            this.protocol = protocol
            this.negotiated = negotiated
        }
        return native.createDataChannel(label, init)?.let { DataChannel(it) }
    }

    actual suspend fun createOffer(options: OfferAnswerOptions): SessionDescription {
        return native.createOffer(RTCOfferOptions().apply {
            this.iceRestart = options.iceRestart == true
        }).asCommon()
    }

    actual suspend fun createAnswer(options: OfferAnswerOptions): SessionDescription {
        return native.createAnswer(RTCAnswerOptions().apply {
            this.voiceActivityDetection = options.voiceActivityDetection == true
        }).asCommon()
    }

    actual suspend fun setLocalDescription(description: SessionDescription) {
        return native.setLocalDescription(description.asNative())
    }

    actual suspend fun setRemoteDescription(description: SessionDescription) {
        return native.setRemoteDescription(description.asNative())
    }

    actual fun setConfiguration(configuration: RtcConfiguration): Boolean {
        native.configuration = configuration.native
        return true
    }

    actual fun addIceCandidate(candidate: IceCandidate): Boolean {
        native.addIceCandidate(candidate.native)
        return true
    }

    actual fun removeIceCandidates(candidates: List<IceCandidate>): Boolean {
        native.removeIceCandidates(candidates.map { it.native }.toTypedArray())
        return true
    }

    actual fun getSenders(): List<RtpSender> = native.senders.map {
        RtpSender(it, localTracks[it.track?.id])
    }

    actual fun getReceivers(): List<RtpReceiver> = native.receivers.map {
        RtpReceiver(it, remoteTracks[it.track?.id])
    }

    actual fun getTransceivers(): List<RtpTransceiver> =
        native.transceivers.map {
            val senderTrack = localTracks[it.sender.track?.id]
            val receiverTrack = remoteTracks[it.receiver.track?.id]
            RtpTransceiver(it, senderTrack, receiverTrack)
        }

    actual fun addTrack(track: MediaStreamTrack, vararg streams: MediaStream): RtpSender {
        require(track is MediaStreamTrackImpl)

        val streamIds = streams.map { it.id }
        localTracks[track.id] = track
        return RtpSender(native.addTrack(track.native, streamIds), track)
    }

    actual fun removeTrack(sender: RtpSender): Boolean {
        localTracks.remove(sender.track?.id)
        native.removeTrack(sender.native)
        return true
    }

    actual suspend fun getStats(): RtcStatsReport? {
        return suspendCoroutine { cont ->
            native.getStats { cont.resume(RtcStatsReport(it)) }
        }
    }

    actual fun close() {
        remoteTracks.values.forEach(MediaStreamTrack::stop)
        remoteTracks.clear()
        native.close()
    }

    internal inner class NativePeerConnectionObserver : PeerConnectionObserver {
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

        override fun onAddStream(nativeStream: NativeMediaStream) {
            // this deprecated API should not longer be used
            // https://developer.mozilla.org/en-US/docs/Web/API/RTCPeerConnection/onaddstream
        }

        override fun onRemoveStream(nativeStream: NativeMediaStream) {
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

        override fun onAddTrack(receiver: RTCRtpReceiver, mediaStreams: Array<out NativeMediaStream>) {
            val transceiver = native.transceivers.find { it.receiver.track.id == receiver.track.id } ?: return
            if (mediaStreams.isEmpty()) return

            val audioTracks = mediaStreams
                .flatMap { it.audioTracks?.toList() ?: emptyList() }
                .map { remoteTracks.getOrPut(it.id) { RemoteAudioStreamTrack(it) } }

            val videoTracks = mediaStreams
                .flatMap { it.videoTracks?.toList() ?: emptyList() }
                .map { remoteTracks.getOrPut(it.id) { RemoteVideoStreamTrack(it) } }

            if(audioTracks.isEmpty() && videoTracks.isEmpty()) {
                return
            }

            val streams = mediaStreams.map { nativeStream ->
                MediaStream(
                    native = nativeStream,
                ).apply {
                    audioTracks.forEach(::addTrack)
                    videoTracks.forEach(::addTrack)
                }
            }

            val senderTrack = localTracks[transceiver.sender.track?.id]
            val receiverTrack = remoteTracks[receiver.track?.id]

            val trackEvent = TrackEvent(
                receiver = RtpReceiver(receiver, receiverTrack),
                streams = streams,
                track = receiverTrack,
                transceiver = RtpTransceiver(transceiver, senderTrack, receiverTrack)
            )

            _peerConnectionEvent.tryEmit(Track(trackEvent))
        }

        override fun onRemoveTrack(receiver: RTCRtpReceiver) {
            val track = remoteTracks.remove(receiver.track?.id)
            _peerConnectionEvent.tryEmit(RemoveTrack(RtpReceiver(receiver, track)))
            track?.stop()
        }

        override fun onTrack(transceiver: RTCRtpTransceiver) {
            transceiver.receiver?.let { receiver ->
                receiver.track?.let { mediaTrack ->
                    val track = when (mediaTrack.kind) {
                        "audio" -> remoteTracks.getOrPut(mediaTrack.id) { RemoteAudioStreamTrack(mediaTrack as AudioTrack) }
                        "video" -> remoteTracks.getOrPut(mediaTrack.id) { RemoteVideoStreamTrack(mediaTrack as VideoTrack) }
                        else -> error("Unknown media stream track kind: $this")
                    }

                    val senderTrack = localTracks[transceiver.sender.track?.id]
                    remoteTracks[track.id] = track

                    val trackEvent = TrackEvent(
                        receiver = RtpReceiver(receiver, track),
                        streams = listOf(
                            MediaStream().apply {
                                remoteTracks.values.forEach(::addTrack)
                            }
                        ),
                        track = track,
                        transceiver = RtpTransceiver(transceiver, senderTrack, track)
                    )

                    _peerConnectionEvent.tryEmit(Track(trackEvent))
                }
            }
        }

        override fun onIceCandidateError(event: RTCPeerConnectionIceErrorEvent) {
            super.onIceCandidateError(event)
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
