package com.shepeliev.webrtckmp

import WebRTC.RTCDataChannel
import WebRTC.RTCDataChannelConfiguration
import WebRTC.RTCMediaConstraints
import WebRTC.RTCPeerConnection
import WebRTC.RTCRtpReceiver
import WebRTC.RTCRtpSender
import WebRTC.RTCRtpTransceiver
import WebRTC.RTCRtpTransceiverInit
import WebRTC.RTCSessionDescription
import WebRTC.dataChannelForLabel
import WebRTC.senderWithKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.native.concurrent.freeze

private typealias CompletionHandler<T> = (result: T?, error: NSError?) -> Unit

actual class PeerConnection internal constructor(
    private val native: RTCPeerConnection,
    actual val events: PeerConnectionEvents,
) {

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
        return sessionDescription(constraints, native::offerForConstraints)
    }

    private suspend fun sessionDescription(
        constraints: MediaConstraints,
        createSdp: (RTCMediaConstraints, CompletionHandler<RTCSessionDescription>) -> Unit
    ): SessionDescription {
        val nativeSdp = suspendCoroutineInternal<RTCSessionDescription> {
            createSdp(constraints.native, it)
        }
        return SessionDescription(nativeSdp!!)
    }

    private suspend inline fun <T> suspendCoroutineInternal(crossinline block: (CompletionHandler<T>) -> Unit): T? {
        return kotlin.coroutines.suspendCoroutine { cont ->
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
                .launchIn(WebRtcKmp.mainScope)
            val completionHandler = { result: T?, error: NSError? ->
                WebRtcKmp.mainScope.launch { resultFlow.emit(Pair(result, error)) }
                Unit
            }
            block(completionHandler.freeze())
        }
    }

    actual suspend fun createAnswer(constraints: MediaConstraints): SessionDescription {
        return sessionDescription(constraints, native::answerForConstraints)
    }

    actual suspend fun setLocalDescription(description: SessionDescription) {
        suspendCoroutineInternal<Unit> { completion ->
            val handler = { error: NSError? -> completion(null, error) }
            native.setLocalDescription(description.native, handler.freeze())
        }
    }

    actual suspend fun setRemoteDescription(description: SessionDescription) {
        suspendCoroutineInternal<Unit> { completion ->
            val handler = { error: NSError? -> completion(null, error) }
            native.setRemoteDescription(description.native, handler.freeze())
        }
    }

    actual fun setAudioPlayout(playout: Boolean) {
        // TODO not implemented
    }

    actual fun setAudioRecording(recording: Boolean) {
        // TODO not implemented
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

    actual fun addStream(stream: MediaStream): Boolean {
        native.addStream(stream.native)
        return true
    }

    actual fun removeStream(stream: MediaStream) = native.removeStream(stream.native)

    actual fun createSender(kind: String, streamId: String): RtpSender? {
        return RtpSender(native.senderWithKind(kind, streamId))
    }

    actual fun getSenders(): List<RtpSender> = native.senders.map { RtpSender(it as RTCRtpSender) }

    actual fun getReceivers(): List<RtpReceiver> =
        native.receivers.map { RtpReceiver(it as RTCRtpReceiver) }

    actual fun getTransceivers(): List<RtpTransceiver> =
        native.transceivers.map { RtpTransceiver(it as RTCRtpTransceiver) }

    actual fun addTrack(track: MediaStreamTrack, streamIds: List<String>): RtpSender {
        return RtpSender(
            native.addTrack(
                (track as BaseMediaStreamTrack).native,
                streamIds.freeze()
            ).freeze()
        )
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

    actual companion object
}
