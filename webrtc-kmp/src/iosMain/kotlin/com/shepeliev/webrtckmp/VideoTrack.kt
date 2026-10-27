@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.com.shepeliev.webrtc.kmp.RTCVideoRendererProtocol

public actual interface VideoTrack : MediaStreamTrack {
    public actual suspend fun switchCamera(deviceId: String?)

    public fun addRenderer(renderer: RTCVideoRendererProtocol)

    public fun removeRenderer(renderer: RTCVideoRendererProtocol)
}
