package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCRtpSender

actual class RtpSender(val native: RTCRtpSender) {
    actual val id: String
        get() = native.senderId()

    actual val track: MediaStreamTrack?
        get() = native.track?.let { BaseMediaStreamTrack.createCommon(it) }

    actual var parameters: RtpParameters
        get() = RtpParameters(native.parameters)
        set(value) {
            native.parameters = value.native
        }

    actual val dtmf: DtmfSender?
        get() = native.dtmfSender?.let { DtmfSender(it) }

    actual fun setTrack(track: MediaStreamTrack?, takeOwnership: Boolean): Boolean {
        native.setTrack((track as? BaseMediaStreamTrack)?.native)
        return true
    }

    actual fun dispose() {
        // not applicable
    }
}
