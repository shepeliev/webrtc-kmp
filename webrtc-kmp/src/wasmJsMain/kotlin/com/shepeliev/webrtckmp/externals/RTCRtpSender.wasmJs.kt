package com.shepeliev.webrtckmp.externals

import com.shepeliev.webrtckmp.internal.await
import org.w3c.dom.mediacapture.MediaStreamTrack
import kotlin.js.Promise

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual suspend fun RTCRtpSender.replaceTrack(withTrack: PlatformMediaStreamTrack?) {
    (this as WasmRTCRtpSender).replaceTrack(withTrack).await<Unit>()
}

@JsName("RTCRtpSender")
internal external interface WasmRTCRtpSender : RTCRtpSender, JsAny {
    fun replaceTrack(newTrack: PlatformMediaStreamTrack?): Promise<MediaStreamTrack?>
}
