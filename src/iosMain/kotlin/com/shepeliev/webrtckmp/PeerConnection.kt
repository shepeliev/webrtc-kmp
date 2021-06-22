package com.shepeliev.webrtckmp

import WebRTC.RTCDataChannelConfiguration
import WebRTC.RTCMediaConstraints
import WebRTC.RTCPeerConnection
import WebRTC.RTCRtpReceiver
import WebRTC.RTCRtpSender
import WebRTC.RTCRtpTransceiver
import WebRTC.dataChannelForLabel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlin.native.concurrent.freeze

actual class PeerConnection actual constructor(rtcConfiguration: RtcConfiguration) {

    val ios: RTCPeerConnection

    actual val localDescription: SessionDescription?
        get() = ios.localDescription?.let { SessionDescription(it) }

    actual val remoteDescription: SessionDescription?
        get() = ios.remoteDescription?.let { SessionDescription(it) }

    actual val signalingState: SignalingState
        get() = rtcSignalingStateAsCommon(ios.signalingState())

    actual val iceConnectionState: IceConnectionState
        get() = rtcIceConnectionStateAsCommon(ios.iceConnectionState())

    actual val connectionState: PeerConnectionState
        get() = rtcPeerConnectionStateAsCommon(ios.connectionState())

    actual val iceGatheringState: IceGatheringState
        get() = rtcIceGatheringStateAsCommon(ios.iceGatheringState())

    internal actual val events: PeerConnectionEvents = PeerConnectionEvents().freeze()

    private val scope = MainScope()

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

    init {
        ios = factory.peerConnectionWithConfiguration(
            rtcConfiguration.native.freeze(),
            RTCMediaConstraints().freeze(),
            PeerConnectionObserver(events).freeze()
        ).freeze()
    }

    actual suspend fun createOffer(options: OfferAnswerOptions): SessionDescription {
        val constraints = options.toRTCMediaConstraints()
        return SessionDescription(ios.awaitResult { offerForConstraints(constraints, it) })
    }

    actual suspend fun createAnswer(options: OfferAnswerOptions): SessionDescription {
        val constraints = options.toRTCMediaConstraints()
        return SessionDescription(ios.awaitResult { answerForConstraints(constraints, it) })
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
        ios.await { setLocalDescription(description.ios, it) }
    }

    actual suspend fun setRemoteDescription(description: SessionDescription) {
        ios.await { setRemoteDescription(description.ios, it) }
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
        scope.cancel()
        ios.close()
    }
}
