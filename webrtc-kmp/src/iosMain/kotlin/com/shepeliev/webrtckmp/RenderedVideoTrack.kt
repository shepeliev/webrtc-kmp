@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.com.shepeliev.webrtc.kmp.RTCVideoRendererProtocol
import swiftPMImport.com.shepeliev.webrtc.kmp.RTCVideoTrack

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
