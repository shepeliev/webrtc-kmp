package com.shepeliev.webrtckmp

import org.webrtc.RtpSender as NativeRtpSender

actual class RtpSender internal constructor(
    val native: NativeRtpSender,
    track: MediaStreamTrack?
) {
    actual val id: String
        get() = native.id()

    private var _track: MediaStreamTrack? = track
    actual val track: MediaStreamTrack? get() = _track

    actual var parameters: RtpParameters
        get() = RtpParameters(native.parameters)
        set(value) {
            native.parameters = value.native
        }

    actual val dtmf: DtmfSender?
        get() = native.dtmf()?.let { DtmfSender(it) }

    actual fun replaceTrack(track: MediaStreamTrack?) {
        native.setTrack((track as? MediaStreamTrackImpl)?.native, true)
        _track = track
    }
}
