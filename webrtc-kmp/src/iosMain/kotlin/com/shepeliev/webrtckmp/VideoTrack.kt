@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCVideoRendererProtocol
import kotlinx.cinterop.ExperimentalForeignApi

public actual interface VideoTrack : MediaStreamTrack {
    public actual suspend fun switchCamera(deviceId: String?)

    public fun addRenderer(renderer: RTCVideoRendererProtocol)

    public fun removeRenderer(renderer: RTCVideoRendererProtocol)
}
