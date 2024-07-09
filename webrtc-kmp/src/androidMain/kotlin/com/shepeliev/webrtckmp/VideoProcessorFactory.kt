package com.shepeliev.webrtckmp

import org.webrtc.VideoProcessor

fun interface VideoProcessorFactory {
    fun createVideoProcessor(): VideoProcessor
}
