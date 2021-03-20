package com.shepeliev.webrtckmm

import android.os.ParcelFileDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.webrtc.SdpObserver
import java.io.File
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.webrtc.CandidatePairChangeEvent as NativeCandidatePairChangeEvent
import org.webrtc.DataChannel as NativeDataChannel
import org.webrtc.IceCandidate as NativeIceCandidate
import org.webrtc.MediaStream as NativeMediaStream
import org.webrtc.PeerConnection as NativePeerConnection
import org.webrtc.RtpReceiver as NativeRtpReceiver
import org.webrtc.RtpTransceiver as NativeRtpTransceiver
import org.webrtc.SessionDescription as NativeSessionDescription

actual class PeerConnection internal constructor() {

    lateinit var native: NativePeerConnection

    actual val localDescription: SessionDescription?
        get() = native.localDescription?.asCommon()

    actual val remoteDescription: SessionDescription?
        get() = native.remoteDescription?.asCommon()

    actual val certificate: RtcCertificatePem?
        get() = native.certificate?.asCommon()

    actual val signalingState: SignalingState
        get() = native.signalingState().asCommon()

    actual val iceConnectionState: IceConnectionState
        get() = native.iceConnectionState().asCommon()

    actual val connectionState: PeerConnectionState
        get() = native.connectionState().asCommon()

    actual val iceGatheringState: IceGatheringState
        get() = native.iceGatheringState().asCommon()

    private val _signalingStateFlow = MutableSharedFlow<SignalingState>()
    actual val signalingStateFlow: Flow<SignalingState> = _signalingStateFlow.asSharedFlow()

    private val _iceConnectionStateFlow = MutableSharedFlow<IceConnectionState>()
    actual val iceConnectionStateFlow: Flow<IceConnectionState> =
        _iceConnectionStateFlow.asSharedFlow()

    private val _connectionStateFlow = MutableSharedFlow<PeerConnectionState>()
    actual val connectionStateFlow: Flow<PeerConnectionState> = _connectionStateFlow.asSharedFlow()

    private val _iceGatheringStateFlow = MutableSharedFlow<IceGatheringState>()
    actual val iceGatheringStateFlow: Flow<IceGatheringState> =
        _iceGatheringStateFlow.asSharedFlow()

    private val _iceCandidateFlow = MutableSharedFlow<IceCandidate>()
    actual val iceCandidateFlow: Flow<IceCandidate> = _iceCandidateFlow.asSharedFlow()

    private val _removedIceCandidatesFlow = MutableSharedFlow<List<IceCandidate>>()
    actual val removedIceCandidatesFlow: Flow<List<IceCandidate>> =
        _removedIceCandidatesFlow.asSharedFlow()

    private val _dataChannelFlow = MutableSharedFlow<DataChannel>()
    actual val dataChannelFlow: Flow<DataChannel> = _dataChannelFlow.asSharedFlow()

    private val _renegotiationNeeded = MutableSharedFlow<Unit>()
    actual val renegotiationNeeded: Flow<Unit> = _renegotiationNeeded.asSharedFlow()

    private val _addStreamFlow = MutableSharedFlow<MediaStream>()
    actual val addStreamFlow: Flow<MediaStream> = _addStreamFlow.asSharedFlow()

    private val _removeStreamFlow = MutableSharedFlow<MediaStream>()
    actual val removeStreamFlow: Flow<MediaStream> = _removeStreamFlow.asSharedFlow()

    private val _addTrackFlow = MutableSharedFlow<Pair<RtpReceiver, List<MediaStream>>>()
    actual val addTrackFlow: Flow<Pair<RtpReceiver, List<MediaStream>>> =
        _addTrackFlow.asSharedFlow()

    private val _removeTrackFlow = MutableSharedFlow<RtpReceiver>()
    actual val removeTrackFlow: Flow<RtpReceiver> = _removeTrackFlow.asSharedFlow()

    internal val pcObserver = PcObserver()

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

    actual fun addStream(stream: MediaStream): Boolean {
        return native.addStream(stream.native)
    }

    actual fun removeStream(stream: MediaStream) {
        native.removeStream(stream.native)
    }

    actual fun createSender(kind: String, streamId: String): RtpSender? {
        return native.createSender(kind, streamId)?.asCommon()
    }

    actual fun getSenders(): List<RtpSender> = native.senders.map { it.asCommon() }
    actual fun getReceivers(): List<RtpReceiver> = native.receivers.map { it.asCommon() }
    actual fun getTransceivers(): List<RtpTransceiver> = native.transceivers.map { it.asCommon() }

    actual fun addTrack(track: MediaStreamTrack, streamIds: List<String>): RtpSender {
        return native.addTrack((track as BaseMediaStreamTrack).native, streamIds).asCommon()
    }

    actual fun removeTrack(sender: RtpSender): Boolean = native.removeTrack(sender.native)

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
        GlobalScope.launch(Dispatchers.Default) { native.dispose() }
    }

    internal inner class PcObserver() : NativePeerConnection.Observer,
        CoroutineScope by MainScope() {
        override fun onSignalingChange(newState: NativePeerConnection.SignalingState) {
            launch { _signalingStateFlow.emit(newState.asCommon()) }
        }

        override fun onIceConnectionChange(newState: NativePeerConnection.IceConnectionState) {
            launch { _iceConnectionStateFlow.emit(newState.asCommon()) }
        }

        override fun onConnectionChange(newState: NativePeerConnection.PeerConnectionState) {
            launch { _connectionStateFlow.emit(newState.asCommon()) }
        }

        override fun onIceConnectionReceivingChange(receiving: Boolean) {}

        override fun onIceGatheringChange(newState: NativePeerConnection.IceGatheringState) {
            launch { _iceGatheringStateFlow.emit(newState.asCommon()) }
        }

        override fun onIceCandidate(candidate: NativeIceCandidate) {
            launch { _iceCandidateFlow.emit(candidate.asCommon()) }
        }

        override fun onIceCandidatesRemoved(candidates: Array<out NativeIceCandidate>) {
            launch { _removedIceCandidatesFlow.emit(candidates.map { it.asCommon() }) }
        }

        override fun onSelectedCandidatePairChanged(event: NativeCandidatePairChangeEvent) {}

        override fun onAddStream(nativeStream: NativeMediaStream) {
            launch { _addStreamFlow.emit(MediaStream(nativeStream)) }
        }

        override fun onRemoveStream(nativeStream: NativeMediaStream) {
            launch { _removeStreamFlow.emit(MediaStream(nativeStream)) }
        }

        override fun onDataChannel(dataChannel: NativeDataChannel) {
            launch { _dataChannelFlow.emit(DataChannel(dataChannel)) }
        }

        override fun onRenegotiationNeeded() {
            launch { _renegotiationNeeded.emit(Unit) }
        }

        override fun onAddTrack(
            receiver: NativeRtpReceiver,
            nativeStreams: Array<out NativeMediaStream>
        ) {
            val streams = nativeStreams.map { MediaStream(it) }
            launch { _addTrackFlow.emit(Pair(RtpReceiver(receiver), streams)) }
        }

        override fun onTrack(transceiver: NativeRtpTransceiver) {}
    }
}

actual fun RtcPeerConnection(rtcConfiguration: RtcConfiguration): PeerConnection {
    return PeerConnection().apply {
        native =
            peerConnectionFactory.native.createPeerConnection(rtcConfiguration.native, pcObserver)!!
    }
}
