package com.shepeliev.webrtckmp

import WebRTC.RTCVideoRendererProtocol
import WebRTC.RTCVideoTrack

actual class VideoStreamTrack internal constructor(
    private val videoTrack: RTCVideoTrack,
    remote: Boolean,
) : MediaStreamTrack(videoTrack, remote) {

    fun addRenderer(renderer: RTCVideoRendererProtocol) {
        videoTrack.addRenderer(renderer)
    }

    fun removeRenderer(renderer: RTCVideoRendererProtocol) {
        videoTrack.removeRenderer(renderer)
    }
}
