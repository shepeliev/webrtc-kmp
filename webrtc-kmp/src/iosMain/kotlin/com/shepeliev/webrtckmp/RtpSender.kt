package com.shepeliev.webrtckmp

import WebRTC.RTCRtpSender

actual class RtpSender(val ios: RTCRtpSender, track: MediaStreamTrack?) {
    actual val id: String
        get() = ios.senderId()

    private var _track: MediaStreamTrack? = track
    actual val track: MediaStreamTrack? get() = _track

    actual var parameters: RtpParameters
        get() = RtpParameters(ios.parameters)
        set(value) {
            ios.parameters = value.native
        }

    actual val dtmf: DtmfSender?
        get() = ios.dtmfSender?.let { DtmfSender(it) }

    actual suspend fun replaceTrack(track: MediaStreamTrack?) {
        ios.setTrack((track as? MediaStreamTrackImpl)?.native)
        _track = track
    }
}
