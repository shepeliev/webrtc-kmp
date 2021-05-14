package com.shepeliev.webrtckmp

import android.os.ParcelFileDescriptor
import com.shepeliev.webrtckmp.WebRtcKmp.mainScope
import kotlinx.coroutines.launch
import org.webrtc.CandidatePairChangeEvent
import org.webrtc.SdpObserver
import java.io.File
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.webrtc.AudioTrack as AndroidAudioTrack
import org.webrtc.DataChannel as NativeDataChannel
import org.webrtc.IceCandidate as NativeIceCandidate
import org.webrtc.MediaStream as NativeMediaStream
import org.webrtc.MediaStreamTrack as AndroidMediaStreamTrack
import org.webrtc.PeerConnection as NativePeerConnection
import org.webrtc.RtpReceiver as NativeRtpReceiver
import org.webrtc.RtpTransceiver as NativeRtpTransceiver
import org.webrtc.SessionDescription as NativeSessionDescription
import org.webrtc.VideoTrack as AndroidVideoTrack

actual class PeerConnection actual constructor(rtcConfiguration: RtcConfiguration) {

    val native: NativePeerConnection

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
        val init = NativeDataChannel.Init().also {
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
            override fun onCreateSuccess(description: NativeSessionDescription) {
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
            override fun onCreateSuccess(description: NativeSessionDescription) {
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

    actual fun setAudioPlayout(playout: Boolean) = native.setAudioPlayout(playout)
    actual fun setAudioRecording(recording: Boolean) = native.setAudioRecording(recording)

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

    actual fun getTransceivers(): List<RtpTransceiver> = native.transceivers.map { it.asCommon() }

    actual fun addTrack(track: MediaStreamTrack, streamIds: List<String>): RtpSender {
        mediaStreamTracks += track.id to track
        return RtpSender(native.addTrack((track as BaseMediaStreamTrack).native, streamIds))
    }

    actual fun removeTrack(sender: RtpSender): Boolean {
        mediaStreamTracks.remove(sender.track?.id)
        return native.removeTrack(sender.native)
    }

    actual fun addTransceiver(
        track: MediaStreamTrack,
        direction: RtpTransceiverDirection,
        streamIds: List<String>,
        sendEncodings: List<RtpParameters.Encoding>
    ): RtpTransceiver {
        return native.addTransceiver(
            (track as BaseMediaStreamTrack).native,
            NativeRtpTransceiver.RtpTransceiverInit(
                direction.asNative(),
                streamIds,
                sendEncodings.map { it.native }
            )
        ).asCommon()
    }

    actual fun addTransceiver(
        mediaType: MediaStreamTrack.MediaType,
        direction: RtpTransceiverDirection,
        streamIds: List<String>,
        sendEncodings: List<RtpParameters.Encoding>
    ): RtpTransceiver {
        return native.addTransceiver(
            mediaType.asNative(),
            NativeRtpTransceiver.RtpTransceiverInit(
                direction.asNative(),
                streamIds,
                sendEncodings.map { it.native }
            )
        ).asCommon()
    }

    actual suspend fun getStats(): RtcStatsReport? {
        return suspendCoroutine { cont ->
            native.getStats { cont.resume(it.asCommon()) }
        }
    }

    actual fun setBitrate(min: Int?, current: Int?, max: Int?): Boolean {
        return native.setBitrate(min, current, max)
    }

    actual fun startRtcEventLog(filePath: String, maxSizeBytes: Int): Boolean {
        val fileDescriptor = ParcelFileDescriptor.open(
            File(filePath),
            ParcelFileDescriptor.MODE_READ_WRITE or
                ParcelFileDescriptor.MODE_CREATE or
                ParcelFileDescriptor.MODE_TRUNCATE
        )
        return native.startRtcEventLog(fileDescriptor.detachFd(), maxSizeBytes)
    }

    actual fun stopRtcEventLog() = native.stopRtcEventLog()

    actual fun close() {
        native.dispose()
    }

    internal inner class PcObserver : NativePeerConnection.Observer {
        override fun onSignalingChange(newState: NativePeerConnection.SignalingState) {
            mainScope.launch { events.onSignalingStateChange.emit(newState.asCommon()) }
        }

        override fun onIceConnectionChange(newState: NativePeerConnection.IceConnectionState) {
            mainScope.launch { events.onIceConnectionStateChange.emit(newState.asCommon()) }
        }

        override fun onStandardizedIceConnectionChange(
            newState: NativePeerConnection.IceConnectionState
        ) {
            mainScope.launch {
                events.onStandardizedIceConnectionChange.emit(newState.asCommon())
            }
        }

        override fun onConnectionChange(newState: NativePeerConnection.PeerConnectionState) {
            mainScope.launch { events.onConnectionStateChange.emit(newState.asCommon()) }
        }

        override fun onIceConnectionReceivingChange(receiving: Boolean) {}

        override fun onIceGatheringChange(newState: NativePeerConnection.IceGatheringState) {
            mainScope.launch { events.onIceGatheringStateChange.emit(newState.asCommon()) }
        }

        override fun onIceCandidate(candidate: NativeIceCandidate) {
            mainScope.launch { events.onIceCandidate.emit(IceCandidate(candidate)) }
        }

        override fun onIceCandidatesRemoved(candidates: Array<out NativeIceCandidate>) {
            mainScope.launch {
                events.onRemovedIceCandidates.emit(candidates.map { IceCandidate(it) })
            }
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

        override fun onDataChannel(dataChannel: NativeDataChannel) {
            mainScope.launch { events.onDataChannel.emit(DataChannel(dataChannel)) }
        }

        override fun onRenegotiationNeeded() {
            mainScope.launch { events.onNegotiationNeeded.emit(Unit) }
        }

        override fun onAddTrack(
            receiver: NativeRtpReceiver,
            nativeStreams: Array<out NativeMediaStream>
        ) {
            // replaced by onTrack
        }

        override fun onTrack(transceiver: NativeRtpTransceiver) {
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

private fun NativePeerConnection.SignalingState.asCommon(): SignalingState {
    return when (this) {
        NativePeerConnection.SignalingState.STABLE -> SignalingState.Stable
        NativePeerConnection.SignalingState.HAVE_LOCAL_OFFER -> SignalingState.HaveLocalOffer
        NativePeerConnection.SignalingState.HAVE_LOCAL_PRANSWER -> SignalingState.HaveLocalPranswer
        NativePeerConnection.SignalingState.HAVE_REMOTE_OFFER -> SignalingState.HaveRemoteOffer
        NativePeerConnection.SignalingState.HAVE_REMOTE_PRANSWER -> SignalingState.HaveRemotePranswer
        NativePeerConnection.SignalingState.CLOSED -> SignalingState.Closed
    }
}

private fun NativePeerConnection.IceConnectionState.asCommon(): IceConnectionState {
    return when (this) {
        NativePeerConnection.IceConnectionState.NEW -> IceConnectionState.New
        NativePeerConnection.IceConnectionState.CHECKING -> IceConnectionState.Checking
        NativePeerConnection.IceConnectionState.CONNECTED -> IceConnectionState.Connected
        NativePeerConnection.IceConnectionState.COMPLETED -> IceConnectionState.Completed
        NativePeerConnection.IceConnectionState.FAILED -> IceConnectionState.Failed
        NativePeerConnection.IceConnectionState.DISCONNECTED -> IceConnectionState.Disconnected
        NativePeerConnection.IceConnectionState.CLOSED -> IceConnectionState.Closed
    }
}

private fun NativePeerConnection.PeerConnectionState.asCommon(): PeerConnectionState {
    return when (this) {
        NativePeerConnection.PeerConnectionState.NEW -> PeerConnectionState.New
        NativePeerConnection.PeerConnectionState.CONNECTING -> PeerConnectionState.Connecting
        NativePeerConnection.PeerConnectionState.CONNECTED -> PeerConnectionState.Connected
        NativePeerConnection.PeerConnectionState.DISCONNECTED -> PeerConnectionState.Disconnected
        NativePeerConnection.PeerConnectionState.FAILED -> PeerConnectionState.Failed
        NativePeerConnection.PeerConnectionState.CLOSED -> PeerConnectionState.Closed
    }
}

private fun NativePeerConnection.IceGatheringState.asCommon(): IceGatheringState {
    return when (this) {
        NativePeerConnection.IceGatheringState.NEW -> IceGatheringState.New
        NativePeerConnection.IceGatheringState.GATHERING -> IceGatheringState.Gathering
        NativePeerConnection.IceGatheringState.COMPLETE -> IceGatheringState.Complete
    }
}
