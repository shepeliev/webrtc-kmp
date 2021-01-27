package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCVideoFrame
import cocoapods.GoogleWebRTC.RTCVideoRendererProtocol
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGSize
import platform.darwin.NSObject

internal class CommonVideoSinkAdapter(private val renderer: VideoRenderer) : NSObject(),
    RTCVideoRendererProtocol {

    override fun renderFrame(frame: RTCVideoFrame?) {
        frame?.let { renderer.onFrame(VideoFrame(it)) }
    }

    override fun setSize(size: CValue<CGSize>) {
        size.useContents { renderer.setSize(Size(width, height)) }
    }
}
