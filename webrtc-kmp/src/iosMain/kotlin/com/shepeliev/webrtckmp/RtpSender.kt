package com.shepeliev.webrtckmp

import WebRTC.RTCRtpSender

actual class RtpSender(val android: RTCRtpSender, track: MediaStreamTrack?) {
    actual val id: String
        get() = android.senderId()

    private var _track: MediaStreamTrack? = track
    actual val track: MediaStreamTrack? get() = _track

    actual var parameters: RtpParameters
        get() = RtpParameters(android.parameters)
        set(value) {
            android.parameters = value.native
        }

    actual val dtmf: DtmfSender?
        get() = android.dtmfSender?.let { DtmfSender(it) }

    actual suspend fun replaceTrack(track: MediaStreamTrack?) {
        android.setTrack((track as? MediaStreamTrackImpl)?.ios)
        _track = track
    }
}
