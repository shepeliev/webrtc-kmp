package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.PlatformMediaStreamTrack

public actual interface VideoStreamTrack : MediaStreamTrack {
    public val js: PlatformMediaStreamTrack

    public actual suspend fun switchCamera(deviceId: String?)
}
