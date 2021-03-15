package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCVideoRendererProtocol
import kotlinx.cinterop.cValue
import platform.CoreGraphics.CGSize

class RTCVideoRendererProtocolAdapter(val native: RTCVideoRendererProtocol) : VideoRenderer {
    override fun onFrame(frame: VideoFrame) = native.renderFrame(frame.native)

    override fun setSize(size: Size) {
        val cvSize = cValue<CGSize> {
            width = size.width
            height = size.height
        }
        native.setSize(cvSize)
    }
}
