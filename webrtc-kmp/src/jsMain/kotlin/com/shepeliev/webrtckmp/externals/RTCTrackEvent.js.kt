package com.shepeliev.webrtckmp.externals

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual val RTCTrackEvent.streams: List<PlatformMediaStream>
    get() = (this as JsRTCTrackEvent).streams.toList()

@JsName("RTCTrackEvent")
private external interface JsRTCTrackEvent : RTCTrackEvent {
    val streams: Array<PlatformMediaStream>
}
