package com.shepeliev.webrtckmp

import org.w3c.dom.mediacapture.MediaStream
import org.w3c.dom.mediacapture.MediaStreamTrack
import kotlin.js.Date
import kotlin.js.Promise

@JsModule("webrtc-adapter")
external object adapter

external class RTCPeerConnection(configuration: dynamic) {
    val localDescription: RTCSessionDescription?
    val remoteDescription: RTCSessionDescription?
    val signalingState: String
    val iceConnectionState: String
    val connectionState: String
    val iceGatheringState: String

    var onsignalingstatechange: (() -> Unit)?
    var oniceconnectionstatechange: (() -> Unit)?
    var onconnectionstatechange: (() -> Unit)?
    var onicegatheringstatechange: (() -> Unit)?
    var onicecandidate: ((RTCPeerConnectionIceEvent) -> Unit)?
    var ondatachannel: ((RTCDataChannelEvent) -> Unit)?
    var onnegotiationneeded: (() -> Unit)?
    var ontrack: ((RTCTrackEvent) -> Unit)?

    fun addIceCandidate(candidate: RTCIceCandidate): Promise<Unit>
    fun addTrack(track: MediaStreamTrack, vararg streams: dynamic): RTCRtpSender
    fun close()
    fun createAnswer(options: dynamic): Promise<RTCSessionDescription>
    fun createDataChannel(label: String, options: dynamic): RTCDataChannel?
    fun createOffer(options: dynamic): Promise<RTCSessionDescription>
    fun getReceivers(): Array<RTCRtpReceiver>
    fun getSenders(): Array<RTCRtpSender>
    fun getStats(): Promise<RTCStatsReport>
    fun getTransceivers(): Array<RTCRtpTransceiver>
    fun removeTrack(sender: RTCRtpSender)
    fun setLocalDescription(sdp: dynamic): Promise<Unit>
    fun setRemoteDescription(sdp: dynamic): Promise<Unit>
    fun setConfiguration(configuration: dynamic)

    companion object {
        fun generateCertificate(algorithm: dynamic): Promise<RTCCertificate>
    }
}

external class RTCCertificate {
    val expires: Date
}

external class RTCStatsReport {
    val id: String
    val timestamp: Double
    val type: String
}

external class RTCSessionDescription {
    val type: String
    val sdp: String
}

external class RTCPeerConnectionIceEvent {
    val candidate: RTCIceCandidate?
}

external class RTCIceCandidate {
    val candidate: String
    val sdpMid: String
    val sdpMLineIndex: Int
}

external class RTCDataChannelEvent {
    val channel: RTCDataChannel
}

external class RTCDataChannel {
    val id: Int
    val label: String
    val readyState: String
    val bufferedAmount: Long

    var onopen: (() -> Unit)?
    var onclose: (() -> Unit)?
    var onclosing: (() -> Unit)?
    var onerror: ((ErrorEvent) -> Unit)?
    var onmessage: ((MessageEvent) -> Unit)?

    fun send(data: dynamic)
    fun close()
}

external class MessageEvent {
    val data: String
}

external class ErrorEvent {
    val message: String
}

external class RTCTrackEvent {
    val receiver: RTCRtpReceiver
    val streams: Array<MediaStream>
    val track: MediaStreamTrack
    val transceiver: RTCRtpTransceiver
}

external class RTCRtpTransceiver {
    val currentDirection: String?
    var direction: String
    val mid: String?
    val receiver: RTCRtpReceiver
    val sender: RTCRtpSender
    val stopped: Boolean

    fun stop()
}

external class RTCRtpSender {
    val dtmf: RTCDTMFSender?
    val track: MediaStreamTrack?

    fun getParameters(): RTCRtpParameters
    fun setParameters(parameters: RTCRtpParameters)
    fun replaceTrack(newTrack: MediaStreamTrack?): Promise<MediaStreamTrack>
}

external class RTCDTMFSender {
    val toneBuffer: String
    fun insertDTMF(tones: String, duration: Long, interToneGap: Long)
}

external class RTCRtpReceiver {
    val track: MediaStreamTrack
    fun getParameters(): RTCRtpParameters
}

external class RTCRtpParameters {
    val codes: Array<RTCRtpCodecParameters>
    val headerExtensions: Array<dynamic>
    val rtcp: RTCRtcpParameters
}

external class RTCRtpCodecParameters {
    val payloadType: Int?
    val mimeType: String?
    val clockRate: Int?
    val channels: Int?
    val sdpFmtpLine: String?
}

external class RTCRtcpParameters {
    val cname: String
    val reducedSize: Boolean
}
