package com.shepeliev.webrtckmp

import WebRTC.RTCVideoCapturerDelegateProtocol
import WebRTC.RTCVideoSource
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
fun interface VideoProcessorFactory {
    fun createVideoProcessor(videoSource: RTCVideoSource): RTCVideoCapturerDelegateProtocol
}
