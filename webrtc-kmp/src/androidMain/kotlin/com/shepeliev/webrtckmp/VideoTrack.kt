package com.shepeliev.webrtckmp

import org.webrtc.VideoSink

public actual interface VideoTrack : MediaStreamTrack {
    public actual suspend fun switchCamera(deviceId: String?)

    public fun addSink(sink: VideoSink)

    public fun removeSink(sink: VideoSink)
}
