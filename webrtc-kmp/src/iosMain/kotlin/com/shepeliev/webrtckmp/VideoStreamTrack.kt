@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCVideoRendererProtocol
import kotlinx.cinterop.ExperimentalForeignApi

actual interface VideoStreamTrack : MediaStreamTrack {
    actual suspend fun switchCamera(deviceId: String?)
    fun addRenderer(renderer: RTCVideoRendererProtocol)
    fun removeRenderer(renderer: RTCVideoRendererProtocol)
}
