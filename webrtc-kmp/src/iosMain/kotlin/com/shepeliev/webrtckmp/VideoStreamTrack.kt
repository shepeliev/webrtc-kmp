package com.shepeliev.webrtckmp

import WebRTC.RTCVideoRendererProtocol

actual interface VideoStreamTrack : MediaStreamTrack {
    actual var shouldReceive: Boolean?
    actual suspend fun switchCamera(deviceId: String?)
    fun addRenderer(renderer: RTCVideoRendererProtocol)
    fun removeRenderer(renderer: RTCVideoRendererProtocol)
}
