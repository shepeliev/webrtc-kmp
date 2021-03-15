package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCDataChannel
import cocoapods.GoogleWebRTC.RTCDataChannelConfiguration
import cocoapods.GoogleWebRTC.RTCIceCandidate
import cocoapods.GoogleWebRTC.RTCIceConnectionState
import cocoapods.GoogleWebRTC.RTCIceGatheringState
import cocoapods.GoogleWebRTC.RTCMediaStream
import cocoapods.GoogleWebRTC.RTCPeerConnection
import cocoapods.GoogleWebRTC.RTCPeerConnectionDelegateProtocol
import cocoapods.GoogleWebRTC.RTCPeerConnectionState
import cocoapods.GoogleWebRTC.RTCRtpReceiver
import cocoapods.GoogleWebRTC.RTCRtpSender
import cocoapods.GoogleWebRTC.RTCRtpTransceiver
import cocoapods.GoogleWebRTC.RTCRtpTransceiverInit
import cocoapods.GoogleWebRTC.RTCSignalingState
import cocoapods.GoogleWebRTC.dataChannelForLabel
import cocoapods.GoogleWebRTC.senderWithKind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import platform.Foundation.NSNumber
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual class PeerConnection internal constructor() :  CoroutineScope by MainScope() {

    lateinit var native: RTCPeerConnection

    actual val localDescription: SessionDescription?
        get() = native.localDescription?.let { SessionDescription(it) }

    actual val remoteDescription: SessionDescription?
        get() = native.remoteDescription?.let { SessionDescription(it) }

    actual val certificate: RtcCertificatePem?
        get() = native.configuration.certificate?.let { RtcCertificatePem(it) }

    actual val signalingState: SignalingState
        get() = rtcSignalingStateAsCommon(native.signalingState())

    actual val iceConnectionState: IceConnectionState
        get() = rtcIceConnectionStateAsCommon(native.iceConnectionState())

    actual val connectionState: PeerConnectionState
        get() = rtcPeerConnectionStateAsCommon(native.connectionState())

    actual val iceGatheringState: IceGatheringState
        get() = rtcIceGatheringStateAsCommon(native.iceGatheringState())

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
        RTCDataChannel()
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

    actual suspend fun createOffer(constraints: MediaConstraints): SessionDescription {
        return suspendCoroutine { cont ->
            native.offerForConstraints(constraints.native) { sdp, error ->
                if (error != null) {
                    val errorText = error.localizedDescription
                    cont.resumeWithException(RuntimeException("Creating SDP failed: $errorText"))
                } else {
                    cont.resume(SessionDescription(sdp!!))
                }
            }
        }
    }

    actual suspend fun createAnswer(constraints: MediaConstraints): SessionDescription {
        return suspendCoroutine { cont ->
            native.answerForConstraints(constraints.native) { sdp, error ->
                if (error != null) {
                    val errorText = error.localizedDescription
                    cont.resumeWithException(RuntimeException("Creating SDP failed: $errorText"))
                } else {
                    cont.resume(SessionDescription(sdp!!))
                }
            }
        }
    }

    actual suspend fun setLocalDescription(description: SessionDescription) {
        return suspendCoroutine { cont ->
            native.setLocalDescription(description.native) { error ->
                if (error != null) {
                    val errorText = error.localizedDescription
                    cont.resumeWithException(RuntimeException("Setting SDP failed:  $errorText"))
                } else {
                    cont.resume(Unit)
                }
            }
        }
    }

    actual suspend fun setRemoteDescription(description: SessionDescription) {
        return suspendCoroutine { cont ->
            native.setRemoteDescription(description.native) { error ->
                if (error != null) {
                    val errorText = error.localizedDescription
                    cont.resumeWithException(RuntimeException("Setting SDP failed:  $errorText"))
                } else {
                    cont.resume(Unit)
                }
            }
        }

    }

    actual fun setAudioPlayout(playout: Boolean) {
        // not implemented
    }

    actual fun setAudioRecording(recording: Boolean) {
        // not implemented
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

//    actual fun addStream(stream: MediaStream): Boolean {
//        native.addStream(stream.native)
//        return true
//    }
//
//    actual fun removeStream(stream: MediaStream) = native.removeStream(stream.native)

    actual fun createSender(kind: String, streamId: String): RtpSender? {
        return RtpSender(native.senderWithKind(kind, streamId))
    }

    actual fun getSenders(): List<RtpSender> = native.senders.map { RtpSender(it as RTCRtpSender) }

    actual fun getReceivers(): List<RtpReceiver> =
        native.receivers.map { RtpReceiver(it as RTCRtpReceiver) }

    actual fun getTransceivers(): List<RtpTransceiver> =
        native.transceivers.map { RtpTransceiver(it as RTCRtpTransceiver) }

    actual fun addTrack(track: MediaStreamTrack, streamIds: List<String>): RtpSender {
        return RtpSender(native.addTrack((track as BaseMediaStreamTrack).native, streamIds))
    }

    actual fun removeTrack(sender: RtpSender): Boolean = native.removeTrack(sender.native)

    actual fun addTransceiver(
        track: MediaStreamTrack,
        direction: RtpTransceiverDirection,
        streamIds: List<String>,
        sendEncodings: List<RtpParameters.Encoding>
    ): RtpTransceiver {
        val init = RTCRtpTransceiverInit().also {
            it.direction = direction.asNative()
            it.streamIds = streamIds
            it.sendEncodings = sendEncodings.map(RtpParameters.Encoding::native)
        }
        return RtpTransceiver(
            native.addTransceiverWithTrack(
                (track as BaseMediaStreamTrack).native,
                init
            )
        )
    }

    actual fun addTransceiver(
        mediaType: MediaStreamTrack.MediaType,
        direction: RtpTransceiverDirection,
        streamIds: List<String>,
        sendEncodings: List<RtpParameters.Encoding>
    ): RtpTransceiver {
        val init = RTCRtpTransceiverInit().also {
            it.direction = direction.asNative()
            it.streamIds = streamIds
            it.sendEncodings = sendEncodings.map(RtpParameters.Encoding::native)
        }
        return RtpTransceiver(
            native.addTransceiverOfType(
                mediaType.asNative(),
                init
            )
        )
    }

    actual suspend fun getStats(): RtcStatsReport? {
        // TODO not implemented yet
        return null
    }

    actual fun setBitrate(min: Int?, current: Int?, max: Int?): Boolean {
        return native.setBweMinBitrateBps(
            min?.let { NSNumber(it) },
            current?.let { NSNumber(it) },
            max?.let { NSNumber(it) }
        )
    }

    actual fun startRtcEventLog(filePath: String, maxSizeBytes: Int): Boolean {
        return native.startRtcEventLogWithFilePath(filePath, maxSizeBytes.toLong())
    }

    actual fun stopRtcEventLog() = native.stopRtcEventLog()
    actual fun close() = native.close()
    actual fun dispose() {
        // not applicable
    }

    private fun rtcSignalingStateAsCommon(state: RTCSignalingState): SignalingState {
        return when (state) {
            RTCSignalingState.RTCSignalingStateStable -> SignalingState.Stable
            RTCSignalingState.RTCSignalingStateHaveLocalOffer -> SignalingState.HaveLocalOffer
            RTCSignalingState.RTCSignalingStateHaveLocalPrAnswer -> SignalingState.HaveLocalPranswer
            RTCSignalingState.RTCSignalingStateHaveRemoteOffer -> SignalingState.HaveRemoteOffer
            RTCSignalingState.RTCSignalingStateHaveRemotePrAnswer -> SignalingState.HaveRemotePranswer
            RTCSignalingState.RTCSignalingStateClosed -> SignalingState.Closed
        }
    }

    private fun rtcIceConnectionStateAsCommon(state: RTCIceConnectionState): IceConnectionState {
        return when (state) {
            RTCIceConnectionState.RTCIceConnectionStateNew -> IceConnectionState.New
            RTCIceConnectionState.RTCIceConnectionStateChecking -> IceConnectionState.Checking
            RTCIceConnectionState.RTCIceConnectionStateConnected -> IceConnectionState.Connected
            RTCIceConnectionState.RTCIceConnectionStateCompleted -> IceConnectionState.Completed
            RTCIceConnectionState.RTCIceConnectionStateFailed -> IceConnectionState.Failed
            RTCIceConnectionState.RTCIceConnectionStateDisconnected -> IceConnectionState.Disconnected
            RTCIceConnectionState.RTCIceConnectionStateClosed -> IceConnectionState.Closed
            RTCIceConnectionState.RTCIceConnectionStateCount -> IceConnectionState.Count
        }
    }

    private fun rtcPeerConnectionStateAsCommon(state: RTCPeerConnectionState): PeerConnectionState {
        return when (state) {
            RTCPeerConnectionState.RTCPeerConnectionStateNew -> PeerConnectionState.New
            RTCPeerConnectionState.RTCPeerConnectionStateConnecting -> PeerConnectionState.Connecting
            RTCPeerConnectionState.RTCPeerConnectionStateConnected -> PeerConnectionState.Connected
            RTCPeerConnectionState.RTCPeerConnectionStateDisconnected -> PeerConnectionState.Disconnected
            RTCPeerConnectionState.RTCPeerConnectionStateFailed -> PeerConnectionState.Failed
            RTCPeerConnectionState.RTCPeerConnectionStateClosed -> PeerConnectionState.Closed
        }
    }

    internal fun rtcIceGatheringStateAsCommon(state: RTCIceGatheringState): IceGatheringState {
        return when (state) {
            RTCIceGatheringState.RTCIceGatheringStateNew -> IceGatheringState.New
            RTCIceGatheringState.RTCIceGatheringStateGathering -> IceGatheringState.Gathering
            RTCIceGatheringState.RTCIceGatheringStateComplete -> IceGatheringState.Complete
        }
    }

    internal inner class PcObserver : NSObject(), RTCPeerConnectionDelegateProtocol {
        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didChangeSignalingState: RTCSignalingState
        ) {
            launch { _signalingStateFlow.emit(rtcSignalingStateAsCommon(didChangeSignalingState)) }
        }

        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didChangeIceConnectionState: RTCIceConnectionState
        ) {
            launch {
                _iceConnectionStateFlow.emit(
                    rtcIceConnectionStateAsCommon(didChangeIceConnectionState)
                )
            }
        }

        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didChangeIceGatheringState: RTCIceGatheringState
        ) {
            launch {
                _iceGatheringStateFlow.emit(rtcIceGatheringStateAsCommon(didChangeIceGatheringState))
            }
        }

        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didGenerateIceCandidate: RTCIceCandidate
        ) {
            launch { _iceCandidateFlow.emit(IceCandidate(didGenerateIceCandidate)) }
        }

        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didRemoveIceCandidates: List<*>
        ) {
            val candidates = didRemoveIceCandidates.map { IceCandidate(it as RTCIceCandidate) }
            launch { _removedIceCandidatesFlow.emit(candidates) }
        }

        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didOpenDataChannel: RTCDataChannel
        ) {
            launch { _dataChannelFlow.emit(DataChannel(didOpenDataChannel)) }
        }

        override fun peerConnectionShouldNegotiate(peerConnection: RTCPeerConnection) {
            launch { _renegotiationNeeded.emit(Unit) }
        }

        @Suppress("CONFLICTING_OVERLOADS")
        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didAddStream: RTCMediaStream
        ) {
        }

        @Suppress("CONFLICTING_OVERLOADS")
        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didRemoveStream: RTCMediaStream
        ) {
        }

        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didChangeConnectionState: RTCPeerConnectionState
        ) {
            launch {
                _connectionStateFlow.emit(rtcPeerConnectionStateAsCommon(didChangeConnectionState))
            }
        }

        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didRemoveReceiver: RTCRtpReceiver
        ) {
            launch { _removeTrackFlow.emit(RtpReceiver(didRemoveReceiver)) }
        }

        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didStartReceivingOnTransceiver: RTCRtpTransceiver
        ) {
        }

        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didAddReceiver: RTCRtpReceiver,
            streams: List<*>
        ) {
            launch {
                _addTrackFlow.emit(
                    Pair(
                        RtpReceiver(didAddReceiver),
                        streams.map { MediaStream(it as RTCMediaStream) }
                    )
                )
            }
        }
    }
}

actual fun RtcPeerConnection(rtcConfiguration: RtcConfiguration): PeerConnection {
    return PeerConnection().apply {
        val constraints = mediaConstraints {
            optional { "RtpDataChannels" to "${rtcConfiguration.enableRtpDataChannel}" }
            rtcConfiguration.enableDtlsSrtp?.let { optional { "DtlsSrtpKeyAgreement" to "$it" } }
        }

        native = peerConnectionFactory.native.peerConnectionWithConfiguration(
            rtcConfiguration.native,
            constraints.native,
            pcObserver
        )
    }
}
