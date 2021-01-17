package com.shepeliev.webrtckmm

import org.webrtc.VideoProcessor as NativeVideoProcessor

actual fun applyFrameAdaptationParameters(
    frame: VideoFrame,
    parameters: VideoProcessor.FrameAdaptationParameters
): VideoFrame? {

    return NativeVideoProcessor.applyFrameAdaptationParameters(
        frame.native,
        NativeVideoProcessor.FrameAdaptationParameters(
            parameters.cropX,
            parameters.cropY,
            parameters.cropWidth,
            parameters.cropHeight,
            parameters.scaleWidth,
            parameters.scaleHeight,
            parameters.timestampNs,
            parameters.drop
        )
    )?.let {
        VideoFrame(it)
    }
}
