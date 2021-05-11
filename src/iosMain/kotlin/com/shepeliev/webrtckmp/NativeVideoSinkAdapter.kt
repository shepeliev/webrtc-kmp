package com.shepeliev.webrtckmp

import WebRTC.RTCVideoRendererProtocol
import kotlinx.cinterop.cValue
import platform.CoreGraphics.CGSize
import platform.Foundation.NSLog

class RTCVideoRendererProtocolAdapter(val native: RTCVideoRendererProtocol) : VideoRenderer {
    override fun onFrame(frame: VideoFrame) {
        NSLog("Render frame")
        native.renderFrame(frame.native)
    }

    override fun setSize(size: Size) {
        val cvSize = cValue<CGSize> {
            width = size.width
            height = size.height
        }
        native.setSize(cvSize)
    }
}
