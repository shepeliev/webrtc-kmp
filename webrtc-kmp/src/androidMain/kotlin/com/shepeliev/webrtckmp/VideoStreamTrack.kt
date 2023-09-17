package com.shepeliev.webrtckmp

import org.webrtc.VideoSink

actual interface VideoStreamTrack : MediaStreamTrack {
    actual var shouldReceive: Boolean?
    actual suspend fun switchCamera(deviceId: String?)
    fun addSink(sink: VideoSink)
    fun removeSink(sink: VideoSink)
}
