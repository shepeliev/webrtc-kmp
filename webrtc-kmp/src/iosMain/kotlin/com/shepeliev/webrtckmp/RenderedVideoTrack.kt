@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCVideoRendererProtocol
import WebRTC.RTCVideoTrack
import kotlinx.cinterop.ExperimentalForeignApi

internal abstract class RenderedVideoTrack(
    private val iosTrack: RTCVideoTrack,
) : MediaStreamTrackImpl(iosTrack),
    VideoTrack {
    override fun addRenderer(renderer: RTCVideoRendererProtocol) {
        iosTrack.addRenderer(renderer)
    }

    override fun removeRenderer(renderer: RTCVideoRendererProtocol) {
        iosTrack.removeRenderer(renderer)
    }
}
