package com.shepeliev.webrtckmp

import org.webrtc.VideoSink

actual interface VideoStreamTrack : MediaStreamTrack {
    actual suspend fun switchCamera(deviceId: String?)
    fun addSink(sink: VideoSink)
    fun removeSink(sink: VideoSink)
}
