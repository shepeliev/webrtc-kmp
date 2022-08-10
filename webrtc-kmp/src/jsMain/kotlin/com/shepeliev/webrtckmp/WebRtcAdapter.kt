@file:JsModule("webrtc-adapter")
@file:JsNonModule

package com.shepeliev.webrtckmp

@JsName("default")
external object WebRtcAdapter {
    val browserDetails: BrowserDetails
}

external interface BrowserDetails {
    val browser: String
    val version: String
}
