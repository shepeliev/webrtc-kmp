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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.webrtc.AudioTrack
import org.webrtc.CandidatePairChangeEvent
import org.webrtc.MediaConstraints
import org.webrtc.MediaStreamTrack.AUDIO_TRACK_KIND
import org.webrtc.MediaStreamTrack.VIDEO_TRACK_KIND
import org.webrtc.SdpObserver
import org.webrtc.VideoTrack
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.webrtc.DataChannel as AndroidDataChannel
import org.webrtc.IceCandidate as AndroidIceCandidate
import org.webrtc.MediaStream as AndroidMediaStream
import org.webrtc.PeerConnection as AndroidPeerConnection
import org.webrtc.RtpReceiver as AndroidRtpReceiver
import org.webrtc.SessionDescription as AndroidSessionDescription

public actual class PeerConnection actual constructor(
    rtcConfiguration: RtcConfiguration,
) {
    public val android: AndroidPeerConnection =
        WebRtc.peerConnectionFactory.createPeerConnection(
            rtcConfiguration.toPlatform(),
            AndroidPeerConnectionObserver(),
        ) ?: error("Creating PeerConnection failed")

    public actual val localDescription: SessionDescription?
        get() = android.localDescription?.asCommon()

    public actual val remoteDescription: SessionDescription?
        get() = android.remoteDescription?.asCommon()

    public actual val signalingState: SignalingState
        get() = if (closed) SignalingState.Closed else android.signalingState().asCommon()

    public actual val iceConnectionState: IceConnectionState
        get() = android.iceConnectionState().asCommon()

    public actual val connectionState: PeerConnectionState
        get() = android.connectionState().asCommon()

    public actual val iceGatheringState: IceGatheringState
        get() = android.iceGatheringState().asCommon()

    @Suppress("ktlint:standard:backing-property-naming")
    private val _peerConnectionEvent =
        MutableSharedFlow<PeerConnectionEvent>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    internal actual val peerConnectionEvent: Flow<PeerConnectionEvent> =
        _peerConnectionEvent
            .asSharedFlow()

    private val coroutineScope = MainScope()
    private val localTracks = mutableMapOf<String, MediaStreamTrackImpl>()
    private val remoteTracks = mutableMapOf<String, MediaStreamTrackImpl>()
    private var closed = false

    public actual fun createDataChannel(
        label: String,
        id: Int,
        ordered: Boolean,
        maxPacketLifeTimeMs: Int,
        maxRetransmits: Int,
        protocol: String,
        negotiated: Boolean,
    ): DataChannel? {
        val init =
            AndroidDataChannel.Init().also {
                it.id = id
                it.ordered = ordered
                it.maxRetransmitTimeMs = maxPacketLifeTimeMs
                it.maxRetransmits = maxRetransmits
                it.protocol = protocol
                it.negotiated = negotiated
            }
        return android.createDataChannel(label, init)?.let { DataChannel(it) }
    }

    public actual suspend fun createOffer(options: OfferAnswerOptions): SessionDescription =
        suspendCoroutine { cont ->
            android.createOffer(createSdpObserver(cont), options.toMediaConstraints())
        }

    public actual suspend fun createAnswer(options: OfferAnswerOptions): SessionDescription =
        suspendCoroutine { cont ->
            android.createAnswer(createSdpObserver(cont), options.toMediaConstraints())
        }

    private fun OfferAnswerOptions.toMediaConstraints(): MediaConstraints =
        MediaConstraints().apply {
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

    private fun createSdpObserver(continuation: Continuation<SessionDescription>): SdpObserver =
        object : SdpObserver {
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

    public actual suspend fun setLocalDescription(description: SessionDescription) {
        suspendCoroutine {
            android.setLocalDescription(setSdpObserver(it), description.asAndroid())
        }
    }

    public actual suspend fun setRemoteDescription(description: SessionDescription) {
        suspendCoroutine {
            android.setRemoteDescription(setSdpObserver(it), description.asAndroid())
        }
    }

    private fun setSdpObserver(continuation: Continuation<Unit>): SdpObserver =
        object : SdpObserver {
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

    public actual fun setConfiguration(configuration: RtcConfiguration): Boolean =
        android.setConfiguration(configuration.toPlatform())

    public actual suspend fun addIceCandidate(candidate: IceCandidate): Boolean =
        android.addIceCandidate(candidate.native)

    public actual fun removeIceCandidates(candidates: List<IceCandidate>): Boolean =
        android.removeIceCandidates(
            candidates
                .map {
                    it.native
                }.toTypedArray(),
        )

    public actual fun getSenders(): List<RtpSender> =
        android.senders.map {
            RtpSender(it, localTracks[it.track()?.id()])
        }

    public actual fun getReceivers(): List<RtpReceiver> =
        android.receivers.map {
            RtpReceiver(it, remoteTracks[it.track()?.id()])
        }

    public actual fun getTransceivers(): List<RtpTransceiver> =
        android.transceivers.map {
            val senderTrack = localTracks[it.sender.track()?.id()]
            val receiverTrack = remoteTracks[it.receiver.track()?.id()]
            RtpTransceiver(it, senderTrack, receiverTrack)
        }

    public actual fun addTrack(
        track: MediaStreamTrack,
        vararg streams: MediaStream,
    ): RtpSender {
        require(track is MediaStreamTrackImpl)

        val streamIds = streams.map { it.id }
        localTracks[track.id] = track
        return RtpSender(android.addTrack(track.android, streamIds), track)
    }

    public actual fun removeTrack(sender: RtpSender): Boolean {
        localTracks.remove(sender.track?.id)
        return android.removeTrack(sender.android)
    }

    public actual suspend fun getStats(): RtcStatsReport? =
        suspendCoroutine { cont ->
            android.getStats { cont.resume(RtcStatsReport(it)) }
        }

    public actual fun close() {
        if (closed) return
        closed = true
        remoteTracks.values.forEach(MediaStreamTrack::stop)
        remoteTracks.clear()
        android.dispose()
        coroutineScope.launch {
            _peerConnectionEvent.emit(SignalingStateChange(SignalingState.Closed))
            coroutineScope.cancel()
        }
    }

    internal inner class AndroidPeerConnectionObserver : AndroidPeerConnection.Observer {
        override fun onSignalingChange(newState: AndroidPeerConnection.SignalingState) {
            coroutineScope.launch {
                _peerConnectionEvent.emit(
                    SignalingStateChange(newState.asCommon()),
                )
            }
        }

        override fun onIceConnectionChange(newState: AndroidPeerConnection.IceConnectionState) {
            coroutineScope.launch {
                _peerConnectionEvent.emit(
                    IceConnectionStateChange(newState.asCommon()),
                )
            }
        }

        override fun onStandardizedIceConnectionChange(
            newState: AndroidPeerConnection.IceConnectionState,
        ) {
            coroutineScope.launch {
                _peerConnectionEvent.emit(StandardizedIceConnectionChange(newState.asCommon()))
            }
        }

        override fun onConnectionChange(newState: AndroidPeerConnection.PeerConnectionState) {
            coroutineScope.launch {
                _peerConnectionEvent.emit(
                    ConnectionStateChange(newState.asCommon()),
                )
            }
        }

        override fun onIceConnectionReceivingChange(receiving: Boolean) {}

        override fun onIceGatheringChange(newState: AndroidPeerConnection.IceGatheringState) {
            coroutineScope.launch {
                _peerConnectionEvent.emit(
                    IceGatheringStateChange(newState.asCommon()),
                )
            }
        }

        override fun onIceCandidate(candidate: AndroidIceCandidate) {
            coroutineScope.launch {
                _peerConnectionEvent.emit(
                    NewIceCandidate(IceCandidate(candidate)),
                )
            }
        }

        override fun onIceCandidatesRemoved(candidates: Array<out AndroidIceCandidate>) {
            coroutineScope.launch {
                _peerConnectionEvent.emit(RemovedIceCandidates(candidates.map { IceCandidate(it) }))
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
            coroutineScope.launch {
                _peerConnectionEvent.emit(
                    NewDataChannel(DataChannel(dataChannel)),
                )
            }
        }

        override fun onRenegotiationNeeded() {
            coroutineScope.launch { _peerConnectionEvent.emit(NegotiationNeeded) }
        }

        override fun onAddTrack(
            receiver: AndroidRtpReceiver,
            androidStreams: Array<out AndroidMediaStream>,
        ) {
            val transceiver =
                android.transceivers.find { it.receiver.id() == receiver.id() } ?: return
            val senderTrack = localTracks[transceiver.sender.track()?.id()]

            val receiverTrack =
                receiver.track()?.let {
                    remoteTracks.getOrPut(it.id()) {
                        when (it.kind()) {
                            AUDIO_TRACK_KIND -> RemoteAudioTrack(it as AudioTrack)
                            VIDEO_TRACK_KIND -> RemoteVideoTrack(it as VideoTrack)
                            else -> error("Unsupported track kind: ${it.kind()}")
                        }
                    }
                }

            val streams =
                androidStreams.map { androidStream ->
                    MediaStream(androidStream).also { stream ->
                        androidStream.audioTracks.forEach {
                            stream.addTrack(RemoteAudioTrack(it))
                        }
                        androidStream.videoTracks.forEach {
                            stream.addTrack(RemoteVideoTrack(it))
                        }
                    }
                }

            val trackEvent =
                TrackEvent(
                    receiver = RtpReceiver(receiver, receiverTrack),
                    streams = streams,
                    track = receiverTrack,
                    transceiver = RtpTransceiver(transceiver, senderTrack, receiverTrack),
                )

            coroutineScope.launch { _peerConnectionEvent.emit(Track(trackEvent)) }
        }

        override fun onRemoveTrack(receiver: AndroidRtpReceiver) {
            val track = remoteTracks.remove(receiver.track()?.id())
            coroutineScope.launch {
                _peerConnectionEvent.emit(
                    RemoveTrack(RtpReceiver(receiver, track)),
                )
            }
            track?.stop()
        }

        override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent) {
            // not implemented
        }
    }
}

private fun AndroidPeerConnection.SignalingState.asCommon(): SignalingState =
    when (this) {
        AndroidPeerConnection.SignalingState.STABLE -> SignalingState.Stable
        AndroidPeerConnection.SignalingState.HAVE_LOCAL_OFFER -> SignalingState.HaveLocalOffer
        AndroidPeerConnection.SignalingState.HAVE_LOCAL_PRANSWER -> SignalingState.HaveLocalPranswer
        AndroidPeerConnection.SignalingState.HAVE_REMOTE_OFFER -> SignalingState.HaveRemoteOffer

        AndroidPeerConnection.SignalingState.HAVE_REMOTE_PRANSWER -> {
            SignalingState.HaveRemotePranswer
        }

        AndroidPeerConnection.SignalingState.CLOSED -> SignalingState.Closed
    }

private fun AndroidPeerConnection.IceConnectionState.asCommon(): IceConnectionState =
    when (this) {
        AndroidPeerConnection.IceConnectionState.NEW -> IceConnectionState.New
        AndroidPeerConnection.IceConnectionState.CHECKING -> IceConnectionState.Checking
        AndroidPeerConnection.IceConnectionState.CONNECTED -> IceConnectionState.Connected
        AndroidPeerConnection.IceConnectionState.COMPLETED -> IceConnectionState.Completed
        AndroidPeerConnection.IceConnectionState.FAILED -> IceConnectionState.Failed
        AndroidPeerConnection.IceConnectionState.DISCONNECTED -> IceConnectionState.Disconnected
        AndroidPeerConnection.IceConnectionState.CLOSED -> IceConnectionState.Closed
    }

private fun AndroidPeerConnection.PeerConnectionState.asCommon(): PeerConnectionState =
    when (this) {
        AndroidPeerConnection.PeerConnectionState.NEW -> PeerConnectionState.New
        AndroidPeerConnection.PeerConnectionState.CONNECTING -> PeerConnectionState.Connecting
        AndroidPeerConnection.PeerConnectionState.CONNECTED -> PeerConnectionState.Connected
        AndroidPeerConnection.PeerConnectionState.DISCONNECTED -> PeerConnectionState.Disconnected
        AndroidPeerConnection.PeerConnectionState.FAILED -> PeerConnectionState.Failed
        AndroidPeerConnection.PeerConnectionState.CLOSED -> PeerConnectionState.Closed
    }

private fun AndroidPeerConnection.IceGatheringState.asCommon(): IceGatheringState =
    when (this) {
        AndroidPeerConnection.IceGatheringState.NEW -> IceGatheringState.New
        AndroidPeerConnection.IceGatheringState.GATHERING -> IceGatheringState.Gathering
        AndroidPeerConnection.IceGatheringState.COMPLETE -> IceGatheringState.Complete
    }
