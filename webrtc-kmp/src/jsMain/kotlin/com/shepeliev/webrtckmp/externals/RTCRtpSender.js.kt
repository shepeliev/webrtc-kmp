package com.shepeliev.webrtckmp.externals

import kotlinx.coroutines.await
import kotlin.js.Promise

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual suspend fun RTCRtpSender.replaceTrack(withTrack: PlatformMediaStreamTrack?) {
    (this as JsRTCRtpSender).replaceTrack(withTrack).await()
}

@JsName("RTCRtpSender")
internal external interface JsRTCRtpSender : RTCRtpSender {
    fun replaceTrack(newTrack: PlatformMediaStreamTrack?): Promise<PlatformMediaStreamTrack?>
}
