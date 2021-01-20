package com.shepeliev.webrtckmm

import org.webrtc.SdpObserver
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.webrtc.DataChannel as NativeDataChannel
import org.webrtc.PeerConnection as NativePeerConnection
import org.webrtc.RtpTransceiver as NativeRtpTransceiver
import org.webrtc.SessionDescription as NativeSessionDescription

actual class PeerConnection internal constructor(val native: NativePeerConnection) {
    actual val localDescription: SessionDescription?
        get() = native.localDescription?.toCommon()

    actual val remoteDescription: SessionDescription?
        get() = native.remoteDescription?.toCommon()

    actual val certificate: RtcCertificatePem?
        get() = native.certificate?.toCommon()

    actual val signalingState: SignalingState
        get() = native.signalingState().toCommon()

    actual val iceConnectionState: IceConnectionState
        get() = native.iceConnectionState().toCommon()

    actual val connectionState: PeerConnectionState
        get() = native.connectionState().toCommon()

    actual val iceGatheringState: IceGatheringState
        get() = native.iceGatheringState().toCommon()

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
                continuation.resume(description.toCommon())
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

    actual fun addStream(stream: MediaStream): Boolean = native.addStream(stream.native)
    actual fun removeStream(stream: MediaStream) = native.removeStream(stream.native)

    actual fun createSender(kind: String, streamId: String): RtpSender? {
        return native.createSender(kind, streamId)?.toCommon()
    }

    actual fun getSenders(): List<RtpSender> = native.senders.map { it.toCommon() }
    actual fun getReceivers(): List<RtpReceiver> = native.receivers.map { it.toCommon() }
    actual fun getTransceivers(): List<RtpTransceiver> = native.transceivers.map { it.toCommon() }

    actual fun addTrack(track: MediaStreamTrack, streamIds: List<String>): RtpSender {
        return native.addTrack((track as BaseMediaStreamTrack).native, streamIds).toCommon()
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
                direction.toNative(),
                streamIds,
                sendEncodings.map { it.native }
            )
        ).toCommon()
    }

    actual fun addTransceiver(
        mediaType: MediaStreamTrack.MediaType,
        direction: RtpTransceiverDirection,
        streamIds: List<String>,
        sendEncodings: List<RtpParameters.Encoding>
    ): RtpTransceiver {
        return native.addTransceiver(
            mediaType.toNative(),
            NativeRtpTransceiver.RtpTransceiverInit(
                direction.toNative(),
                streamIds,
                sendEncodings.map { it.native }
            )
        ).toCommon()
    }

    actual suspend fun getStats(): RtcStatsReport {
        return suspendCoroutine { cont ->
            native.getStats { cont.resume(it.toCommon()) }
        }
    }

    actual fun setBitrate(min: Int?, current: Int?, max: Int?): Boolean {
        return native.setBitrate(min, current, max)
    }

    actual fun startRtcEventLog(fileDescriptor: Int, maxSizeBytes: Int): Boolean {
        return native.startRtcEventLog(fileDescriptor, maxSizeBytes)
    }

    actual fun stopRtcEventLog() = native.stopRtcEventLog()
    actual fun close() = native.close()
    actual fun dispose() = native.dispose()
}
