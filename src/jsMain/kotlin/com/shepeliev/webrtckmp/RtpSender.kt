package com.shepeliev.webrtckmp

import kotlinx.coroutines.await
import com.shepeliev.webrtckmp.MediaStreamTrack as JsMediaStreamTrack

actual class RtpSender(val js: RTCRtpSender) {
    actual val id: String
        get() = track?.id ?: ""

    actual val track: JsMediaStreamTrack?
        get() = js.track?.asCommon()

    actual var parameters: RtpParameters
        get() = RtpParameters(js.getParameters())
        set(value) = js.setParameters(value.js)

    actual val dtmf: DtmfSender?
        get() = js.dtmf?.let { DtmfSender(it) }

    actual suspend fun replaceTrack(track: JsMediaStreamTrack?) {
        js.replaceTrack(track?.js).await()
    }
}
