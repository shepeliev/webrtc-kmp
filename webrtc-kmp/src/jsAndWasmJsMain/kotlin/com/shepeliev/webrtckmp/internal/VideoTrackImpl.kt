package com.shepeliev.webrtckmp.internal

import com.shepeliev.webrtckmp.MediaStreamTrackImpl
import com.shepeliev.webrtckmp.VideoTrack
import com.shepeliev.webrtckmp.externals.PlatformMediaStreamTrack

internal class VideoTrackImpl(
    override val js: PlatformMediaStreamTrack,
) : MediaStreamTrackImpl(js),
    VideoTrack {
    override suspend fun switchCamera(deviceId: String?) {
        Console.warn("switchCamera is not supported in browser environment.")
    }
}
