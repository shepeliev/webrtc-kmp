package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCDataChannel
import cocoapods.GoogleWebRTC.RTCDataChannelConfiguration
import cocoapods.GoogleWebRTC.RTCPeerConnection
import cocoapods.GoogleWebRTC.RTCRtpReceiver
import cocoapods.GoogleWebRTC.RTCRtpSender
import cocoapods.GoogleWebRTC.RTCRtpTransceiver
import cocoapods.GoogleWebRTC.RTCRtpTransceiverInit
import cocoapods.GoogleWebRTC.dataChannelForLabel
import cocoapods.GoogleWebRTC.senderWithKind
import cocoapods.GoogleWebRTC.statisticsWithCompletionHandler
import cocoapods.GoogleWebRTC.statsForTrack
import kotlinx.cinterop.usePinned
import platform.Foundation.NSNumber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual class PeerConnection internal constructor(val native: RTCPeerConnection) {
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
}
