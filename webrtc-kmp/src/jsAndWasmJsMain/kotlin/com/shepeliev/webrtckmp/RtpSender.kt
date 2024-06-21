package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.RTCRtpSender
import com.shepeliev.webrtckmp.externals.replaceTrack

actual class RtpSender internal constructor(internal val js: RTCRtpSender) {
    actual val id: String
        get() = track?.id ?: ""

    actual val track: MediaStreamTrack?
        get() = js.track?.let { MediaStreamTrackImpl(it) }

    actual var parameters: RtpParameters
        get() = RtpParameters(js.getParameters())
        set(value) = js.setParameters(value.platform)

    actual val dtmf: DtmfSender?
        get() = js.dtmf?.let { DtmfSender(it) }

    actual suspend fun replaceTrack(track: MediaStreamTrack?) {
        js.replaceTrack((track as? MediaStreamTrackImpl)?.platform)
    }
}
