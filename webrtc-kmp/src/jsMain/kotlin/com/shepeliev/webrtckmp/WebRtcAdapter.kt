@file:JsModule("webrtc-adapter")
@file:JsNonModule

package com.shepeliev.webrtckmp

@JsName("default")
public external object WebRtcAdapter {
    public val browserDetails: BrowserDetails
}

public external interface BrowserDetails {
    public val browser: String
    public val version: String
}
