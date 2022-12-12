package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.video.VideoTrackSink


actual interface VideoStreamTrack : MediaStreamTrack {
    actual suspend fun switchCamera(deviceId: String?)
    fun addSink(sink: VideoTrackSink)
    fun removeSink(sink: VideoTrackSink)
}
