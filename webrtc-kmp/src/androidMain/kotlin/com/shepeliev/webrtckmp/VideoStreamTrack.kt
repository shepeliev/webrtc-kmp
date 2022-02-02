package com.shepeliev.webrtckmp

import org.webrtc.VideoSink
import org.webrtc.VideoTrack

actual class VideoStreamTrack internal constructor(
    android: VideoTrack,
    private val onSwitchCamera: suspend (String?) -> Unit = { },
    private val onStop: () -> Unit = { },
) : MediaStreamTrack(android) {

    actual suspend fun switchCamera(deviceId: String?) {
        onSwitchCamera(deviceId)
    }

    fun addSink(sink: VideoSink) {
        (android as VideoTrack).addSink(sink)
    }

    fun removeSink(sink: VideoSink) {
        (android as VideoTrack).removeSink(sink)
    }

    override fun stop() {
        onStop()
        super.stop()
    }
}
