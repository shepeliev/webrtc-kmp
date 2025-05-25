package com.shepeliev.webrtckmp

import org.webrtc.VideoSink

public actual interface VideoStreamTrack : MediaStreamTrack {
    public actual suspend fun switchCamera(deviceId: String?)

    public fun addSink(sink: VideoSink)

    public fun removeSink(sink: VideoSink)
}
