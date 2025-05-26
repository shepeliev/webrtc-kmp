package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.PlatformMediaStreamTrack

public actual interface VideoTrack : MediaStreamTrack {
    public val js: PlatformMediaStreamTrack

    public actual suspend fun switchCamera(deviceId: String?)
}
