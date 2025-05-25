package com.shepeliev.webrtckmp

import WebRTC.RTCVideoCapturerDelegateProtocol
import WebRTC.RTCVideoSource
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
public fun interface VideoProcessorFactory {
    public fun createVideoProcessor(videoSource: RTCVideoSource): RTCVideoCapturerDelegateProtocol
}
