package com.shepeliev.webrtckmp

import org.w3c.dom.mediacapture.MediaStreamTrack as DomMediaStreamTrack


actual interface VideoStreamTrack : MediaStreamTrack {
    val js: DomMediaStreamTrack
    actual suspend fun switchCamera(deviceId: String?)
}
