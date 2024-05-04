package com.shepeliev.webrtckmp.externals

import com.shepeliev.webrtckmp.internal.toList
import org.w3c.dom.mediacapture.MediaStream

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual val RTCTrackEvent.streams: List<PlatformMediaStream>
    get() = (this as WasmJsRTCTrackEvent).streams.toList().map { it as PlatformMediaStream }

@JsName("RTCTrackEvent")
private external interface WasmJsRTCTrackEvent : RTCTrackEvent {
    val streams: JsArray<MediaStream>
}
