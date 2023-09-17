package com.shepeliev.webrtckmp

import kotlinx.coroutines.await

actual class RtpSender(val js: RTCRtpSender) {
    actual val id: String
        get() = track?.id ?: ""

    actual val track: MediaStreamTrack?
        get() = js.track?.asCommon()

    actual var parameters: RtpParameters
        get() = RtpParameters(js.getParameters())
        set(value) = js.setParameters(value.js)

    actual val dtmf: DtmfSender?
        get() = js.dtmf?.let { DtmfSender(it) }

    actual suspend fun replaceTrack(track: MediaStreamTrack?) {
        js.replaceTrack((track as? MediaStreamTrackImpl)?.native).await()
    }
}
