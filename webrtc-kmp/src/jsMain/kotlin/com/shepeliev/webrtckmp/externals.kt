package com.shepeliev.webrtckmp

import org.khronos.webgl.ArrayBuffer
import org.w3c.dom.mediacapture.MediaStream
import org.w3c.dom.mediacapture.MediaStreamTrack
import kotlin.js.Date
import kotlin.js.Promise

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
    fun addTrack(track: MediaStreamTrack, vararg streams: MediaStream): RTCRtpSender
    fun addTransceiver(track: MediaStreamTrack, init: dynamic): RTCRtpTransceiver
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

external class RTCIceCandidate(candidateInfo: dynamic) {
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
    val data: ArrayBuffer
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

    fun setCodecPreferences(codecs: Array<RTCRtpCodecCapability>)
    fun stop()
}

external class RTCRtpSender {
    val dtmf: RTCDTMFSender?
    val track: MediaStreamTrack?

    fun getParameters(): RTCRtpParameters
    fun setParameters(parameters: RTCRtpParameters)
    fun replaceTrack(newTrack: MediaStreamTrack?): Promise<MediaStreamTrack>
    fun getCapabilities(kind: String): RTCRtpCapabilities
}

external class RTCDTMFSender {
    val toneBuffer: String
    fun insertDTMF(tones: String, duration: Long, interToneGap: Long)
}

external class RTCRtpReceiver {
    val track: MediaStreamTrack
    fun getParameters(): RTCRtpParameters
    fun getCapabilities(kind: String): RTCRtpCapabilities
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

external class RTCRtpCapabilities {
    val codecs: Array<RTCRtpCodecCapability>
    val headerExtensions: Array<RTCRtpHeaderExtension>
}

external class RTCRtpCodecCapability {
    val channels: Int?
    val clockRate: Int
    val mimeType: String
    val sdpFmtpLine: String?
}

external class RTCRtpHeaderExtension {
    val uri: String
}
