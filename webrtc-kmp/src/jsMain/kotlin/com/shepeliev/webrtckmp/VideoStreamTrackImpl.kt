package com.shepeliev.webrtckmp

import org.w3c.dom.mediacapture.MediaStreamTrack as JsMediaStreamTrack

internal class VideoStreamTrackImpl(
    js: JsMediaStreamTrack
) : MediaStreamTrackImpl(js), VideoStreamTrack {
    override suspend fun switchCamera(deviceId: String?) {
        console.warn("switchCamera is not supported in browser")
    }
}
