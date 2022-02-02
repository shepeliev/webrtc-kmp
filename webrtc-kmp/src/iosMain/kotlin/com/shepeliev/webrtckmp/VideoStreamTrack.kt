package com.shepeliev.webrtckmp

import WebRTC.RTCVideoRendererProtocol
import WebRTC.RTCVideoTrack

actual class VideoStreamTrack internal constructor(
    ios: RTCVideoTrack,
    private val onSwitchCamera: (String?) -> Unit = { },
    private val onStop: () -> Unit = { },
) : MediaStreamTrack(ios) {

    fun addRenderer(renderer: RTCVideoRendererProtocol) {
        (ios as RTCVideoTrack).addRenderer(renderer)
    }

    fun removeRenderer(renderer: RTCVideoRendererProtocol) {
        (ios as RTCVideoTrack).removeRenderer(renderer)
    }

    actual suspend fun switchCamera(deviceId: String?) {
        onSwitchCamera(deviceId)
    }

    override fun stop() {
        onStop()
        super.stop()
    }
}
