package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.CreateSessionDescriptionObserver
import dev.onvoid.webrtc.PeerConnectionObserver
import dev.onvoid.webrtc.RTCAnswerOptions
import dev.onvoid.webrtc.RTCBundlePolicy
import dev.onvoid.webrtc.RTCDataChannel
import dev.onvoid.webrtc.RTCDataChannelInit
import dev.onvoid.webrtc.RTCIceCandidate
import dev.onvoid.webrtc.RTCIceConnectionState
import dev.onvoid.webrtc.RTCIceGatheringState
import dev.onvoid.webrtc.RTCOfferOptions
import dev.onvoid.webrtc.RTCPeerConnection
import dev.onvoid.webrtc.RTCPeerConnectionState
import dev.onvoid.webrtc.RTCRtpReceiver
import dev.onvoid.webrtc.RTCSessionDescription
import dev.onvoid.webrtc.RTCSignalingState
import dev.onvoid.webrtc.SetSessionDescriptionObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual class PeerConnection actual constructor(rtcConfiguration: RtcConfiguration) {

    val jvm: RTCPeerConnection = WebRtc.peerConnectionFactory.createPeerConnection(
        rtcConfiguration.jvm,
        JvmPeerConnectionObserver()
    ) ?: error("Creating PeerConnection failed")

    actual val localDescription: SessionDescription?
        get() = jvm.localDescription?.asCommon()

    actual val remoteDescription: SessionDescription?
        get() = jvm.remoteDescription?.asCommon()

    actual val signalingState: SignalingState
        get() = jvm.signalingState.asCommon()

    actual val iceConnectionState: IceConnectionState
        get() = jvm.iceConnectionState.asCommon()

    actual val connectionState: PeerConnectionState
        get() = jvm.connectionState.asCommon()

    actual val iceGatheringState: IceGatheringState
        get() = jvm.iceGatheringState.asCommon()

    private val _peerConnectionEvent =
        MutableSharedFlow<PeerConnectionEvent>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    internal actual val peerConnectionEvent: Flow<PeerConnectionEvent> =
        _peerConnectionEvent.asSharedFlow()

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
        val init = RTCDataChannelInit().also {
            it.id = id
            it.ordered = ordered
            it.maxPacketLifeTime = maxRetransmitTimeMs
            it.maxRetransmits = maxRetransmits
            it.protocol = protocol
            it.negotiated = negotiated
        }
        return jvm.createDataChannel(label, init)?.let { DataChannel(it) }
    }

    actual suspend fun createOffer(options: OfferAnswerOptions): SessionDescription {
        return suspendCoroutine { cont ->
            jvm.createOffer(
                RTCOfferOptions().apply {
                    options.iceRestart?.let { iceRestart = it }
                    options.voiceActivityDetection?.let { voiceActivityDetection = it }
                },
                createSdpObserver(cont)
            )
        }
    }

    actual suspend fun createAnswer(options: OfferAnswerOptions): SessionDescription {
        return suspendCoroutine { cont ->
            jvm.createAnswer(
                RTCAnswerOptions().apply {
                    options.voiceActivityDetection?.let { voiceActivityDetection = it }
                },
                createSdpObserver(cont)
            )
        }
    }

    private fun createSdpObserver(continuation: Continuation<SessionDescription>): CreateSessionDescriptionObserver {
        return object : CreateSessionDescriptionObserver {
            override fun onSuccess(description: RTCSessionDescription?) {
                description?.asCommon()?.let { continuation.resume(it) }
            }

            override fun onFailure(error: String?) {
                continuation.resumeWithException(RuntimeException("Creating SDP failed: $error"))
            }
        }
    }

    actual suspend fun setLocalDescription(description: SessionDescription) {
        return suspendCoroutine {
            jvm.setLocalDescription(
                description.asNative(),
                setSdpObserver(it)
            )
        }
    }

    actual suspend fun setRemoteDescription(description: SessionDescription) {
        return suspendCoroutine {
            jvm.setRemoteDescription(
                description.asNative(),
                setSdpObserver(it)
            )
        }
    }

    private fun setSdpObserver(continuation: Continuation<Unit>): SetSessionDescriptionObserver {
        return object : SetSessionDescriptionObserver {
            override fun onSuccess() {
                continuation.resume(Unit)
            }

            override fun onFailure(error: String?) {
                continuation.resumeWithException(RuntimeException("Setting SDP failed: $error"))
            }
        }
    }

    actual fun setConfiguration(configuration: RtcConfiguration): Boolean {
        return try {
            jvm.configuration = configuration.jvm
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    actual fun addIceCandidate(candidate: IceCandidate): Boolean {
        return try {
            jvm.addIceCandidate(candidate.native)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    actual fun removeIceCandidates(candidates: List<IceCandidate>): Boolean {
        return try {
            jvm.removeIceCandidates(candidates.map { it.native }.toTypedArray())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    actual fun getSenders(): List<RtpSender> = jvm.senders.map {
        RtpSender(it, localTracks[it.track?.id])
    }

    actual fun getReceivers(): List<RtpReceiver> = jvm.receivers.map {
        RtpReceiver(it, remoteTracks[it.track?.id])
    }

    actual fun getTransceivers(): List<RtpTransceiver> = jvm.transceivers.map {
        val senderTrack = localTracks[it.sender.track?.id]
        RtpTransceiver(it, senderTrack)
    }

    actual fun addTrack(track: MediaStreamTrack, vararg streams: MediaStream): RtpSender {
        val streamIds = streams.map { it.id }
        localTracks[track.id] = track
        return RtpSender(jvm.addTrack(track.native, streamIds), track)
    }

    actual fun removeTrack(sender: RtpSender): Boolean {
        return try {
            localTracks.remove(sender.track?.id)
            jvm.removeTrack(sender.native)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    actual suspend fun getStats(): RtcStatsReport? {
        return suspendCoroutine { cont ->
            jvm.getStats { cont.resume(RtcStatsReport(it)) }
        }
    }

    actual fun close() {
        remoteTracks.values.forEach(MediaStreamTrack::stop)
        remoteTracks.clear()
        jvm.close()
    }

    internal inner class JvmPeerConnectionObserver : PeerConnectionObserver {
        override fun onSignalingChange(newState: RTCSignalingState) {
            _peerConnectionEvent.tryEmit(PeerConnectionEvent.SignalingStateChange(newState.asCommon()))
        }

        override fun onIceConnectionChange(newState: RTCIceConnectionState) {
            _peerConnectionEvent.tryEmit(PeerConnectionEvent.IceConnectionStateChange(newState.asCommon()))
        }

        override fun onStandardizedIceConnectionChange(newState: RTCIceConnectionState) {
            _peerConnectionEvent.tryEmit(
                PeerConnectionEvent.StandardizedIceConnectionChange(
                    newState.asCommon()
                )
            )
        }

        override fun onConnectionChange(newState: RTCPeerConnectionState) {
            _peerConnectionEvent.tryEmit(PeerConnectionEvent.ConnectionStateChange(newState.asCommon()))
        }

        override fun onIceConnectionReceivingChange(receiving: Boolean) {}

        override fun onIceGatheringChange(newState: RTCIceGatheringState) {
            _peerConnectionEvent.tryEmit(PeerConnectionEvent.IceGatheringStateChange(newState.asCommon()))
        }

        override fun onIceCandidate(candidate: RTCIceCandidate) {
            _peerConnectionEvent.tryEmit(PeerConnectionEvent.NewIceCandidate(IceCandidate(candidate)))
        }

        override fun onIceCandidatesRemoved(candidates: Array<out RTCIceCandidate>) {
            _peerConnectionEvent.tryEmit(PeerConnectionEvent.RemovedIceCandidates(candidates.map {
                IceCandidate(
                    it
                )
            }))
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
            _peerConnectionEvent.tryEmit(PeerConnectionEvent.NewDataChannel(DataChannel(dataChannel)))
        }

        override fun onRenegotiationNeeded() {
            _peerConnectionEvent.tryEmit(PeerConnectionEvent.NegotiationNeeded)
        }

        override fun onAddTrack(
            receiver: RTCRtpReceiver,
            nativeStreams: Array<out dev.onvoid.webrtc.media.MediaStream>
        ) {
            val transceiver = jvm.transceivers.find { it.receiver == receiver } ?: return

            val audioTracks = nativeStreams
                .flatMap { it.audioTracks.toList() }
                .map { remoteTracks.getOrPut(it.id) { AudioStreamTrack(it) } }

            val videoTracks = nativeStreams
                .flatMap { it.videoTracks.toList() }
                .map { remoteTracks.getOrPut(it.id) { VideoStreamTrack(it) } }

            val streams = nativeStreams.map { nativeStream ->
                MediaStream(
                    native = nativeStream,
                    id = nativeStream.id(),
                ).also { stream ->
                    audioTracks.forEach(stream::addTrack)
                    videoTracks.forEach(stream::addTrack)
                }
            }

            val senderTrack = localTracks[transceiver.sender.track?.id]
            val receiverTrack = remoteTracks[receiver.track?.id]

            val trackEvent = TrackEvent(
                receiver = RtpReceiver(receiver, receiverTrack),
                streams = streams,
                track = receiverTrack,
                transceiver = RtpTransceiver(transceiver, senderTrack)
            )

            _peerConnectionEvent.tryEmit(PeerConnectionEvent.Track(trackEvent))
        }

        override fun onRemoveTrack(receiver: RTCRtpReceiver) {
            val track = remoteTracks.remove(receiver.track?.id)
            _peerConnectionEvent.tryEmit(
                PeerConnectionEvent.RemoveTrack(RtpReceiver(receiver, track))
            )
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

private fun BundlePolicy.asNative(): RTCBundlePolicy {
    return when (this) {
        BundlePolicy.MaxBundle -> RTCBundlePolicy.MAX_BUNDLE
        BundlePolicy.Balanced -> RTCBundlePolicy.BALANCED
        BundlePolicy.MaxCompat -> RTCBundlePolicy.MAX_COMPAT
    }
}
