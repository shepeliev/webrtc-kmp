package com.shepeliev.webrtckmp

import org.webrtc.VideoSink
import org.webrtc.VideoTrack

actual class VideoStreamTrack internal constructor(
    android: VideoTrack,
    private val onSwitchCamera: suspend (String?) -> Unit = { },
    private val onTrackSetEnabled: (Boolean) -> Unit = { },
    private val onTrackStopped: () -> Unit = { },
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

    override fun onSetEnabled(enabled: Boolean) {
        onTrackSetEnabled(enabled)
    }

    override fun onStop() {
        onTrackStopped()
    }
}
