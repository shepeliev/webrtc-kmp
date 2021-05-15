package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.WebRtcKmp.mainScope
import kotlinx.coroutines.launch
import org.webrtc.CandidatePairChangeEvent
import org.webrtc.SdpObserver
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.webrtc.AudioTrack as AndroidAudioTrack
import org.webrtc.DataChannel as AndroidDataChannel
import org.webrtc.IceCandidate as AndroidIceCandidate
import org.webrtc.MediaStream as AndroidMediaStream
import org.webrtc.MediaStreamTrack as AndroidMediaStreamTrack
import org.webrtc.PeerConnection as AndroidPeerConnection
import org.webrtc.RtpReceiver as AndroidRtpReceiver
import org.webrtc.RtpTransceiver as AndroidRtpTransceiver
import org.webrtc.SessionDescription as AndroidSessionDescription
import org.webrtc.VideoTrack as AndroidVideoTrack

actual class PeerConnection actual constructor(rtcConfiguration: RtcConfiguration) {

    val native: AndroidPeerConnection

    actual val localDescription: SessionDescription?
        get() = native.localDescription?.let { SessionDescription(it) }

    actual val remoteDescription: SessionDescription?
        get() = native.remoteDescription?.let { SessionDescription(it) }

    actual val certificate: RtcCertificatePem?
        get() = native.certificate?.let { RtcCertificatePem(it) }

    actual val signalingState: SignalingState
        get() = native.signalingState().asCommon()

    actual val iceConnectionState: IceConnectionState
        get() = native.iceConnectionState().asCommon()

    actual val connectionState: PeerConnectionState
        get() = native.connectionState().asCommon()

    actual val iceGatheringState: IceGatheringState
        get() = native.iceGatheringState().asCommon()

    internal actual val events = PeerConnectionEvents()

    private val pcObserver = PcObserver()

    private val mediaStreamTracks = mutableMapOf<String, MediaStreamTrack>()

    init {
        native = WebRtcKmp.peerConnectionFactory.native
            .createPeerConnection(rtcConfiguration.native, pcObserver)
            ?: error("Creating PeerConnection failed")
    }

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
        return native.createDataChannel(label, init)?.let { DataChannel(it) }
    }

    actual suspend fun createOffer(constraints: MediaConstraints): SessionDescription {
        return suspendCoroutine { cont ->
            native.createOffer(createSdpObserver(cont), constraints.native)
        }
    }

    actual suspend fun createAnswer(constraints: MediaConstraints): SessionDescription {
        return suspendCoroutine { cont ->
            native.createAnswer(createSdpObserver(cont), constraints.native)
        }
    }

    private fun createSdpObserver(continuation: Continuation<SessionDescription>): SdpObserver {
        return object : SdpObserver {
            override fun onCreateSuccess(description: AndroidSessionDescription) {
                continuation.resume(SessionDescription(description))
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
            native.setLocalDescription(setSdpObserver(it), description.native)
        }
    }

    actual suspend fun setRemoteDescription(description: SessionDescription) {
        return suspendCoroutine {
            native.setRemoteDescription(setSdpObserver(it), description.native)
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
        return native.setConfiguration(configuration.native)
    }

    actual fun addIceCandidate(candidate: IceCandidate): Boolean {
        return native.addIceCandidate(candidate.native)
    }

    actual fun removeIceCandidates(candidates: List<IceCandidate>): Boolean {
        return native.removeIceCandidates(candidates.map { it.native }.toTypedArray())
    }

    actual fun getSenders(): List<RtpSender> = native.senders.map { RtpSender(it) }

    actual fun getReceivers(): List<RtpReceiver> = native.receivers.map { RtpReceiver(it) }

    actual fun getTransceivers(): List<RtpTransceiver> =
        native.transceivers.map { RtpTransceiver(it) }

    actual fun addTrack(track: MediaStreamTrack, streamIds: List<String>): RtpSender {
        mediaStreamTracks += track.id to track
        return RtpSender(native.addTrack((track as BaseMediaStreamTrack).native, streamIds))
    }

    actual fun removeTrack(sender: RtpSender): Boolean {
        mediaStreamTracks.remove(sender.track?.id)
        return native.removeTrack(sender.native)
    }

    actual suspend fun getStats(): RtcStatsReport? {
        return suspendCoroutine { cont ->
            native.getStats { cont.resume(it.asCommon()) }
        }
    }

    actual fun close() {
        native.dispose()
    }

    internal inner class PcObserver : AndroidPeerConnection.Observer {
        override fun onSignalingChange(newState: AndroidPeerConnection.SignalingState) {
            mainScope.launch { events.onSignalingStateChange.emit(newState.asCommon()) }
        }

        override fun onIceConnectionChange(newState: AndroidPeerConnection.IceConnectionState) {
            mainScope.launch { events.onIceConnectionStateChange.emit(newState.asCommon()) }
        }

        override fun onStandardizedIceConnectionChange(
            newState: AndroidPeerConnection.IceConnectionState
        ) {
            mainScope.launch {
                events.onStandardizedIceConnectionChange.emit(newState.asCommon())
            }
        }

        override fun onConnectionChange(newState: AndroidPeerConnection.PeerConnectionState) {
            mainScope.launch { events.onConnectionStateChange.emit(newState.asCommon()) }
        }

        override fun onIceConnectionReceivingChange(receiving: Boolean) {}

        override fun onIceGatheringChange(newState: AndroidPeerConnection.IceGatheringState) {
            mainScope.launch { events.onIceGatheringStateChange.emit(newState.asCommon()) }
        }

        override fun onIceCandidate(candidate: AndroidIceCandidate) {
            mainScope.launch { events.onIceCandidate.emit(IceCandidate(candidate)) }
        }

        override fun onIceCandidatesRemoved(candidates: Array<out AndroidIceCandidate>) {
            mainScope.launch {
                events.onRemovedIceCandidates.emit(candidates.map { IceCandidate(it) })
            }
        }

        override fun onAddStream(nativeStream: AndroidMediaStream) {
            // this deprecated API should not longer be used
            // https://developer.mozilla.org/en-US/docs/Web/API/RTCPeerConnection/onaddstream
        }

        override fun onRemoveStream(nativeStream: AndroidMediaStream) {
            // The removestream event has been removed from the WebRTC specification in favor of
            // the existing removetrack event on the remote MediaStream and the corresponding
            // MediaStream.onremovetrack event handler property of the remote MediaStream.
            // The RTCPeerConnection API is now track-based, so having zero tracks in the remote
            // stream is equivalent to the remote stream being removed and the old removestream event.
            // https://developer.mozilla.org/en-US/docs/Web/API/RTCPeerConnection/onremovestream
        }

        override fun onDataChannel(dataChannel: AndroidDataChannel) {
            mainScope.launch { events.onDataChannel.emit(DataChannel(dataChannel)) }
        }

        override fun onRenegotiationNeeded() {
            mainScope.launch { events.onNegotiationNeeded.emit(Unit) }
        }

        override fun onAddTrack(
            receiver: AndroidRtpReceiver,
            nativeStreams: Array<out AndroidMediaStream>
        ) {
            // replaced by onTrack
        }

        override fun onTrack(transceiver: AndroidRtpTransceiver) {
            val sender = transceiver.sender
            val track = when (transceiver.mediaType) {
                AndroidMediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO -> {
                    AudioTrack(transceiver.receiver.track() as AndroidAudioTrack)
                }
                AndroidMediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO -> {
                    VideoTrack(transceiver.receiver.track() as AndroidVideoTrack)
                }
                else -> null
            }
            val streams = sender.streams.map { id ->
                MediaStream(id).apply { track?.also { addTrack(it) } }
            }
            val trackEvent = TrackEvent(
                receiver = RtpReceiver(transceiver.receiver),
                streams = streams,
                track = track,
                transceiver = RtpTransceiver(transceiver)
            )
            mainScope.launch { events.onTrack.emit(trackEvent) }
        }

        override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent) {
            // not implemented
        }
    }
}

private fun AndroidPeerConnection.SignalingState.asCommon(): SignalingState {
    return when (this) {
        AndroidPeerConnection.SignalingState.STABLE -> SignalingState.Stable
        AndroidPeerConnection.SignalingState.HAVE_LOCAL_OFFER -> SignalingState.HaveLocalOffer
        AndroidPeerConnection.SignalingState.HAVE_LOCAL_PRANSWER -> SignalingState.HaveLocalPranswer
        AndroidPeerConnection.SignalingState.HAVE_REMOTE_OFFER -> SignalingState.HaveRemoteOffer
        AndroidPeerConnection.SignalingState.HAVE_REMOTE_PRANSWER -> SignalingState.HaveRemotePranswer
        AndroidPeerConnection.SignalingState.CLOSED -> SignalingState.Closed
    }
}

private fun AndroidPeerConnection.IceConnectionState.asCommon(): IceConnectionState {
    return when (this) {
        AndroidPeerConnection.IceConnectionState.NEW -> IceConnectionState.New
        AndroidPeerConnection.IceConnectionState.CHECKING -> IceConnectionState.Checking
        AndroidPeerConnection.IceConnectionState.CONNECTED -> IceConnectionState.Connected
        AndroidPeerConnection.IceConnectionState.COMPLETED -> IceConnectionState.Completed
        AndroidPeerConnection.IceConnectionState.FAILED -> IceConnectionState.Failed
        AndroidPeerConnection.IceConnectionState.DISCONNECTED -> IceConnectionState.Disconnected
        AndroidPeerConnection.IceConnectionState.CLOSED -> IceConnectionState.Closed
    }
}

private fun AndroidPeerConnection.PeerConnectionState.asCommon(): PeerConnectionState {
    return when (this) {
        AndroidPeerConnection.PeerConnectionState.NEW -> PeerConnectionState.New
        AndroidPeerConnection.PeerConnectionState.CONNECTING -> PeerConnectionState.Connecting
        AndroidPeerConnection.PeerConnectionState.CONNECTED -> PeerConnectionState.Connected
        AndroidPeerConnection.PeerConnectionState.DISCONNECTED -> PeerConnectionState.Disconnected
        AndroidPeerConnection.PeerConnectionState.FAILED -> PeerConnectionState.Failed
        AndroidPeerConnection.PeerConnectionState.CLOSED -> PeerConnectionState.Closed
    }
}

private fun AndroidPeerConnection.IceGatheringState.asCommon(): IceGatheringState {
    return when (this) {
        AndroidPeerConnection.IceGatheringState.NEW -> IceGatheringState.New
        AndroidPeerConnection.IceGatheringState.GATHERING -> IceGatheringState.Gathering
        AndroidPeerConnection.IceGatheringState.COMPLETE -> IceGatheringState.Complete
    }
}
