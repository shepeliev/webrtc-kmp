package com.shepeliev.webrtckmp

import WebRTC.RTCRtpSender

actual class RtpSender(val native: RTCRtpSender, track: MediaStreamTrack?) {
    actual val id: String
        get() = native.senderId()

    private var _track: MediaStreamTrack? = track
    actual val track: MediaStreamTrack? get() = _track

    actual var parameters: RtpParameters
        get() = RtpParameters(native.parameters)
        set(value) {
            native.parameters = value.native
        }

    actual val dtmf: DtmfSender?
        get() = native.dtmfSender?.let { DtmfSender(it) }

    actual suspend fun replaceTrack(track: MediaStreamTrack?) {
        native.setTrack(track?.ios)
        _track = track
    }
}
