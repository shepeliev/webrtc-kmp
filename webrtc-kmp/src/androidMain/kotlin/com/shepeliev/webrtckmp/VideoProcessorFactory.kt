package com.shepeliev.webrtckmp

import org.webrtc.VideoProcessor

public fun interface VideoProcessorFactory {
    public fun createVideoProcessor(): VideoProcessor
}
