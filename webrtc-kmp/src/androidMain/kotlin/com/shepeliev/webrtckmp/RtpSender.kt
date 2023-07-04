package com.shepeliev.webrtckmp

import org.webrtc.RtpSender as NativeRtpSender

actual class RtpSender internal constructor(
    val android: NativeRtpSender,
    track: MediaStreamTrack?
) {
    actual val id: String
        get() = android.id()

    private var _track: MediaStreamTrack? = track
    actual val track: MediaStreamTrack? get() = _track

    actual var parameters: RtpParameters
        get() = RtpParameters(android.parameters)
        set(value) {
            android.parameters = value.native
        }

    actual val dtmf: DtmfSender?
        get() = android.dtmf()?.let { DtmfSender(it) }

    actual suspend fun replaceTrack(track: MediaStreamTrack?) {
        android.setTrack((track as? MediaStreamTrackImpl)?.android, true)
        _track = track
    }
}
