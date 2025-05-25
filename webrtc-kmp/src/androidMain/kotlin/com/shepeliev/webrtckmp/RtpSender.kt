package com.shepeliev.webrtckmp

import org.webrtc.RtpSender as NativeRtpSender

public actual class RtpSender internal constructor(
    public val android: NativeRtpSender,
    track: MediaStreamTrack?,
) {
    public actual val id: String get() = android.id()

    private var _track: MediaStreamTrack? = track
    public actual val track: MediaStreamTrack? get() = _track

    public actual var parameters: RtpParameters
        get() = RtpParameters(android.parameters)
        set(value) {
            android.parameters = value.native
        }

    public actual val dtmf: DtmfSender? get() = android.dtmf()?.let { DtmfSender(it) }

    public actual suspend fun replaceTrack(track: MediaStreamTrack?) {
        android.setTrack(
            (track as? MediaStreamTrackImpl)?.android,
            false, // ownership MUST be managed by the caller
        )
        _track = track
    }
}
