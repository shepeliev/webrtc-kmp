package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack
import WebRTC.RTCDataChannel
import WebRTC.RTCDataChannelConfiguration
import WebRTC.RTCIceCandidate
import WebRTC.RTCIceConnectionState
import WebRTC.RTCIceGatheringState
import WebRTC.RTCMediaConstraints
import WebRTC.RTCMediaStream
import WebRTC.RTCPeerConnection
import WebRTC.RTCPeerConnectionDelegateProtocol
import WebRTC.RTCPeerConnectionState
import WebRTC.RTCRtpMediaType
import WebRTC.RTCRtpReceiver
import WebRTC.RTCRtpSender
import WebRTC.RTCRtpTransceiver
import WebRTC.RTCSessionDescription
import WebRTC.RTCSignalingState
import WebRTC.RTCVideoTrack
import WebRTC.dataChannelForLabel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.darwin.NSObject
import kotlin.native.concurrent.freeze

actual class PeerConnection actual constructor(rtcConfiguration: RtcConfiguration) {

    val ios: RTCPeerConnection = factory.peerConnectionWithConfiguration(
        configuration = rtcConfiguration.native,
        constraints = RTCMediaConstraints(),
        delegate = IosPeerConnectionObserver()
    )

    actual val localDescription: SessionDescription? get() = ios.localDescription?.asCommon()

    actual val remoteDescription: SessionDescription? get() = ios.remoteDescription?.asCommon()

    actual val signalingState: SignalingState
        get() = rtcSignalingStateAsCommon(ios.signalingState())

    actual val iceConnectionState: IceConnectionState
        get() = rtcIceConnectionStateAsCommon(ios.iceConnectionState())

    actual val connectionState: PeerConnectionState
        get() = rtcPeerConnectionStateAsCommon(ios.connectionState())

    actual val iceGatheringState: IceGatheringState
        get() = rtcIceGatheringStateAsCommon(ios.iceGatheringState())

    private val peerConnectionObserverProxy = PeerConnectionObserverProxy()

    internal actual val peerConnectionEvent: Flow<PeerConnectionEvent> = callbackFlow {
        val observer = object : PeerConnectionObserver {
            override fun onSignalingStateChange(state: SignalingState) {
                trySendBlocking(PeerConnectionEvent.SignalingStateChange(state))
            }

            override fun onIceConnectionStateChange(state: IceConnectionState) {
                trySendBlocking(PeerConnectionEvent.IceConnectionStateChange(state))
            }

            override fun onStandardizedIceConnectionChange(state: IceConnectionState) {
                trySendBlocking(PeerConnectionEvent.StandardizedIceConnectionChange(state))
            }

            override fun onConnectionStateChange(state: PeerConnectionState) {
                trySendBlocking(PeerConnectionEvent.ConnectionStateChange(state))
            }

            override fun onIceGatheringStateChange(state: IceGatheringState) {
                trySendBlocking(PeerConnectionEvent.IceGatheringStateChange(state))
            }

            override fun onIceCandidate(candidate: IceCandidate) {
                trySendBlocking(PeerConnectionEvent.NewIceCandidate(candidate))
            }

            override fun onRemovedIceCandidates(candidates: List<IceCandidate>) {
                trySendBlocking(PeerConnectionEvent.RemovedIceCandidates(candidates))
            }

            override fun onDataChannel(dataChannel: DataChannel) {
                trySendBlocking(PeerConnectionEvent.NewDataChannel(dataChannel))
            }

            override fun onRemoveTrack(rtpReceiver: RtpReceiver) {
                trySendBlocking(PeerConnectionEvent.RemoveTrack(rtpReceiver))
            }

            override fun onNegotiationNeeded() {
                trySendBlocking(PeerConnectionEvent.NegotiationNeeded)
            }

            override fun onTrack(trackEvent: TrackEvent) {
                trySendBlocking(PeerConnectionEvent.Track(trackEvent))
            }
        }

        peerConnectionObserverProxy.addObserver(observer)

        awaitClose { peerConnectionObserverProxy.removeObserver(observer) }
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
        val config = RTCDataChannelConfiguration().also {
            it.channelId = id
            it.isOrdered = ordered
            it.maxRetransmitTimeMs = maxRetransmitTimeMs.toLong()
            it.maxRetransmits = maxRetransmits
            it.protocol = protocol
            it.isNegotiated = negotiated
        }
        return ios.dataChannelForLabel(label, config)?.let { DataChannel(it.freeze()) }
    }

    actual suspend fun createOffer(options: OfferAnswerOptions): SessionDescription {
        val constraints = options.toRTCMediaConstraints()
        val sessionDescription: RTCSessionDescription = ios.awaitResult {
            offerForConstraints(constraints, it)
        }
        return sessionDescription.asCommon()
    }

    actual suspend fun createAnswer(options: OfferAnswerOptions): SessionDescription {
        val constraints = options.toRTCMediaConstraints()
        val sessionDescription: RTCSessionDescription = ios.awaitResult {
            answerForConstraints(constraints, it)
        }
        return sessionDescription.asCommon()
    }

    private fun OfferAnswerOptions.toRTCMediaConstraints(): RTCMediaConstraints {
        val mandatory = mutableMapOf<Any?, String?>().apply {
            iceRestart?.let { this += "iceRestart" to "$it" }
            offerToReceiveAudio?.let { this += "offerToReceiveAudio" to "$it" }
            offerToReceiveVideo?.let { this += "offerToReceiveVideo" to "$it" }
            voiceActivityDetection?.let { this += "voiceActivityDetection" to "$it" }
        }
        return RTCMediaConstraints(mandatory, null)
    }

    actual suspend fun setLocalDescription(description: SessionDescription) {
        ios.await { setLocalDescription(description.asIos(), it) }
    }

    actual suspend fun setRemoteDescription(description: SessionDescription) {
        ios.await { setRemoteDescription(description.asIos(), it) }
    }

    actual fun setConfiguration(configuration: RtcConfiguration): Boolean {
        return ios.setConfiguration(configuration.native)
    }

    actual fun addIceCandidate(candidate: IceCandidate): Boolean {
        ios.addIceCandidate(candidate.native)
        return true
    }

    actual fun removeIceCandidates(candidates: List<IceCandidate>): Boolean {
        ios.removeIceCandidates(candidates.map { it.native })
        return true
    }

    actual fun getSenders(): List<RtpSender> = ios.senders.map { RtpSender(it as RTCRtpSender) }

    actual fun getReceivers(): List<RtpReceiver> =
        ios.receivers.map { RtpReceiver(it as RTCRtpReceiver) }

    actual fun getTransceivers(): List<RtpTransceiver> =
        ios.transceivers.map { RtpTransceiver(it as RTCRtpTransceiver) }

    actual fun addTrack(track: MediaStreamTrack, vararg streams: MediaStream): RtpSender {
        val streamIds = streams.map { it.id }
        return RtpSender(ios.addTrack(track.ios, streamIds.freeze()).freeze())
    }

    actual fun removeTrack(sender: RtpSender): Boolean = ios.removeTrack(sender.native)

    actual suspend fun getStats(): RtcStatsReport? {
        // TODO not implemented yet
        return null
    }

    actual fun close() {
        ios.close()
    }

    private inner class IosPeerConnectionObserver() : NSObject(), RTCPeerConnectionDelegateProtocol {

        override fun peerConnection(peerConnection: RTCPeerConnection, didChangeSignalingState: RTCSignalingState) {
            peerConnectionObserverProxy.onSignalingStateChange(rtcSignalingStateAsCommon(didChangeSignalingState))
        }

        @Suppress("CONFLICTING_OVERLOADS")
        override fun peerConnection(peerConnection: RTCPeerConnection, didAddStream: RTCMediaStream) {
            // this deprecated API should not longer be used
            // https://developer.mozilla.org/en-US/docs/Web/API/RTCPeerConnection/onaddstream
        }

        @Suppress("CONFLICTING_OVERLOADS")
        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didRemoveStream: RTCMediaStream
        ) {
            // The removestream event has been removed from the WebRTC specification in favor of
            // the existing removetrack event on the remote MediaStream and the corresponding
            // MediaStream.onremovetrack event handler property of the remote MediaStream.
            // The RTCPeerConnection API is now track-based, so having zero tracks in the remote
            // stream is equivalent to the remote stream being removed and the old removestream event.
            // https://developer.mozilla.org/en-US/docs/Web/API/RTCPeerConnection/onremovestream
        }

        @Suppress("CONFLICTING_OVERLOADS")
        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didChangeIceConnectionState: RTCIceConnectionState
        ) {
            peerConnectionObserverProxy.onIceConnectionStateChange(
                rtcIceConnectionStateAsCommon(didChangeIceConnectionState)
            )
        }

        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didChangeIceGatheringState: RTCIceGatheringState
        ) {
            peerConnectionObserverProxy.onIceGatheringStateChange(
                rtcIceGatheringStateAsCommon(didChangeIceGatheringState)
            )
        }

        override fun peerConnection(peerConnection: RTCPeerConnection, didGenerateIceCandidate: RTCIceCandidate) {
            peerConnectionObserverProxy.onIceCandidate(IceCandidate(didGenerateIceCandidate))
        }

        override fun peerConnection(peerConnection: RTCPeerConnection, didRemoveIceCandidates: List<*>) {
            val candidates = didRemoveIceCandidates.map { IceCandidate(it as RTCIceCandidate) }
            peerConnectionObserverProxy.onRemovedIceCandidates(candidates)
        }

        override fun peerConnection(peerConnection: RTCPeerConnection, didOpenDataChannel: RTCDataChannel) {
            peerConnectionObserverProxy.onDataChannel(DataChannel(didOpenDataChannel))
        }

        @Suppress("CONFLICTING_OVERLOADS")
        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didChangeStandardizedIceConnectionState: RTCIceConnectionState
        ) {
            peerConnectionObserverProxy.onStandardizedIceConnectionChange(
                rtcIceConnectionStateAsCommon(didChangeStandardizedIceConnectionState)
            )
        }

        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didChangeConnectionState: RTCPeerConnectionState
        ) {
            peerConnectionObserverProxy.onConnectionStateChange(
                rtcPeerConnectionStateAsCommon(didChangeConnectionState)
            )
        }

        override fun peerConnection(peerConnection: RTCPeerConnection, didRemoveReceiver: RTCRtpReceiver) {
            peerConnectionObserverProxy.onRemoveTrack(RtpReceiver(didRemoveReceiver))
        }

        override fun peerConnection(
            peerConnection: RTCPeerConnection,
            didStartReceivingOnTransceiver: RTCRtpTransceiver
        ) {
            val sender = didStartReceivingOnTransceiver.sender
            val receiver = didStartReceivingOnTransceiver.receiver

            val track = when (didStartReceivingOnTransceiver.mediaType) {
                RTCRtpMediaType.RTCRtpMediaTypeAudio -> AudioStreamTrack(receiver.track as RTCAudioTrack)
                RTCRtpMediaType.RTCRtpMediaTypeVideo -> VideoStreamTrack(receiver.track as RTCVideoTrack)
                RTCRtpMediaType.RTCRtpMediaTypeData, RTCRtpMediaType.RTCRtpMediaTypeUnsupported -> null
                else -> error("Unknown RTCRtpMediaType: ${didStartReceivingOnTransceiver.mediaType}")
            }

            val tracks = track?.let { listOf(it) } ?: emptyList()

            val streams = sender.streamIds.takeIf { it.isNotEmpty() }
                ?.map { id -> MediaStream(ios = null, "$id", tracks) }
                ?: listOf(MediaStream(tracks))

            val trackEvent = TrackEvent(
                receiver = RtpReceiver(didStartReceivingOnTransceiver.receiver),
                streams = streams,
                track = track,
                transceiver = RtpTransceiver(didStartReceivingOnTransceiver)
            )

            peerConnectionObserverProxy.onTrack(trackEvent)
        }

        override fun peerConnectionShouldNegotiate(peerConnection: RTCPeerConnection) {
            peerConnectionObserverProxy.onNegotiationNeeded()
        }
    }
}
