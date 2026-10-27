package com.shepeliev.webrtckmp

import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.com.shepeliev.webrtc.kmp.RTCVideoCapturerDelegateProtocol
import swiftPMImport.com.shepeliev.webrtc.kmp.RTCVideoSource

@OptIn(ExperimentalForeignApi::class)
public fun interface VideoProcessorFactory {
    public fun createVideoProcessor(videoSource: RTCVideoSource): RTCVideoCapturerDelegateProtocol
}
