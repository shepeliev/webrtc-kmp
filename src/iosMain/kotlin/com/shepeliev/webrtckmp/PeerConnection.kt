package com.shepeliev.webrtckmp

import WebRTC.RTCDataChannel
import WebRTC.RTCDataChannelConfiguration
import WebRTC.RTCMediaConstraints
import WebRTC.RTCPeerConnection
import WebRTC.RTCRtpReceiver
import WebRTC.RTCRtpSender
import WebRTC.RTCRtpTransceiver
import WebRTC.RTCSessionDescription
import WebRTC.dataChannelForLabel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import platform.Foundation.NSError
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.native.concurrent.freeze

private typealias CompletionHandler<T> = (result: T?, error: NSError?) -> Unit

actual class PeerConnection actual constructor(rtcConfiguration: RtcConfiguration) {

    val ios: RTCPeerConnection

    actual val localDescription: SessionDescription?
        get() = ios.localDescription?.let { SessionDescription(it) }

    actual val remoteDescription: SessionDescription?
        get() = ios.remoteDescription?.let { SessionDescription(it) }

    actual val certificate: RtcCertificatePem?
        get() = ios.configuration.certificate?.let { RtcCertificatePem(it) }

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
        RTCDataChannel()
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
        return sessionDescription(options, ios::offerForConstraints)
    }

    actual suspend fun createAnswer(options: OfferAnswerOptions): SessionDescription {
        return sessionDescription(options, ios::answerForConstraints)
    }

    private suspend fun sessionDescription(
        options: OfferAnswerOptions,
        createSdp: (RTCMediaConstraints, CompletionHandler<RTCSessionDescription>) -> Unit
    ): SessionDescription {
        val nativeSdp = suspendCoroutineInternal<RTCSessionDescription> {
            createSdp(options.toRTCMediaConstraints(), it)
        }
        return SessionDescription(nativeSdp!!)
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

    // TODO: replace the workaround after resolving https://github.com/Kotlin/kotlinx.coroutines/issues/2363
    private suspend inline fun <T> suspendCoroutineInternal(crossinline block: (CompletionHandler<T>) -> Unit): T? {
        return suspendCoroutine { cont ->
            val resultFlow = MutableStateFlow<Pair<T?, NSError?>?>(null).freeze()
            resultFlow
                .filterNotNull()
                .onEach { (result, error) ->
                    if (error != null) {
                        val errorText = error.localizedDescription
                        cont.resumeWithException(RuntimeException(errorText))
                    } else {
                        cont.resume(result)
                    }
                }
                .launchIn(scope)
            val completionHandler = { result: T?, error: NSError? ->
                scope.launch { resultFlow.emit(Pair(result, error)) }
                Unit
            }
            block(completionHandler.freeze())
        }
    }

    actual suspend fun setLocalDescription(description: SessionDescription) {
        suspendCoroutineInternal<Unit> { completion ->
            val handler = { error: NSError? -> completion(null, error) }
            ios.setLocalDescription(description.native, handler.freeze())
        }
    }

    actual suspend fun setRemoteDescription(description: SessionDescription) {
        suspendCoroutineInternal<Unit> { completion ->
            val handler = { error: NSError? -> completion(null, error) }
            ios.setRemoteDescription(description.native, handler.freeze())
        }
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

    actual fun addTrack(track: MediaStreamTrack, streamIds: List<String>): RtpSender {
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
