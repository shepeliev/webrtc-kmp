package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.PlatformMediaStreamTrack

actual interface VideoStreamTrack : MediaStreamTrack {
    val js: PlatformMediaStreamTrack
    actual suspend fun switchCamera(deviceId: String?)
}
