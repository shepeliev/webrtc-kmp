package com.shepeliev.webrtckmp.externals

external interface RTCRtpCodecParameters {
    val payloadType: Int?
    val mimeType: String?
    val clockRate: Int?
    val channels: Int?
    val sdpFmtpLine: String?
}
