package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.RTCRtpSender
import com.shepeliev.webrtckmp.externals.replaceTrack

public actual class RtpSender internal constructor(
    internal val js: RTCRtpSender,
) {
    public actual val id: String get() = track?.id ?: ""
    public actual val track: MediaStreamTrack? get() = js.track?.let { MediaStreamTrackImpl(it) }

    public actual var parameters: RtpParameters
        get() = RtpParameters(js.getParameters())
        set(value) = js.setParameters(value.platform)

    public actual val dtmf: DtmfSender? get() = js.dtmf?.let { DtmfSender(it) }

    public actual suspend fun replaceTrack(track: MediaStreamTrack?) {
        js.replaceTrack((track as? MediaStreamTrackImpl)?.platform)
    }
}
