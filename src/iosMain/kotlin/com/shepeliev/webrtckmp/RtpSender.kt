package com.shepeliev.webrtckmp

import WebRTC.RTCRtpSender

actual class RtpSender(val native: RTCRtpSender) {
    actual val id: String
        get() = native.senderId()

    actual val track: MediaStreamTrack?
        get() = native.track?.let { MediaStreamTrack.createCommon(it, remote = false) }

    actual var parameters: RtpParameters
        get() = RtpParameters(native.parameters)
        set(value) {
            native.parameters = value.native
        }

    actual val dtmf: DtmfSender?
        get() = native.dtmfSender?.let { DtmfSender(it) }

    actual suspend fun replaceTrack(track: MediaStreamTrack?) {
        native.setTrack(track?.native)
    }
}
