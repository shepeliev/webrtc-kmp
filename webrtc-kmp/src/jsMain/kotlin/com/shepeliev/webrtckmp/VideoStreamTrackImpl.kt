@file:Suppress("UNUSED_PARAMETER")

package com.shepeliev.webrtckmp

import org.w3c.dom.mediacapture.MediaStreamTrack as JsMediaStreamTrack

internal class VideoStreamTrackImpl(
    js: JsMediaStreamTrack
) : MediaStreamTrackImpl(js), VideoStreamTrack {
    override var shouldReceive: Boolean?
        get() = null // undefined in JS
        set(value) {
            console.warn("shouldReceive is not supported in JS")
        }

    override suspend fun switchCamera(deviceId: String?) {
        console.warn("switchCamera is not supported in browser")
    }
}
