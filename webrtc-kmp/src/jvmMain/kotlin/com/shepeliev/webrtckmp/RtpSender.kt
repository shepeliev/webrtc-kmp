package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCRtpSender

actual class RtpSender internal constructor(val native: RTCRtpSender, track: MediaStreamTrack?) {
    actual val id: String
        get() = native.id()

    private var _track: MediaStreamTrack? = track
    actual val track: MediaStreamTrack? get() = _track

    actual var parameters: RtpParameters
        get() = RtpParameters(native.parameters)
        set(value) {
            native.parameters = value.native.rtcp
        }

    actual val dtmf: DtmfSender?
        get() = native.dtmf()?.let { DtmfSender(it) }

    actual suspend fun replaceTrack(track: MediaStreamTrack?) {
        native.replaceTrack(track?.jvm)
        //native.setTrack(track?.jvm, true)
        _track = track
    }
}
