package com.shepeliev.webrtckmm

interface VideoProcessor : CapturerObserver {
    fun onFrameCaptured(frame: VideoFrame, parameters: FrameAdaptationParameters) {
        val adaptedFrame = applyFrameAdaptationParameters(frame, parameters)
        if (adaptedFrame != null) {
            onFrameCaptured(adaptedFrame)
            adaptedFrame.release()
        }
    }

    fun setSink(sink: VideoSink?)

    data class FrameAdaptationParameters(
        val cropX: Int,
        val cropY: Int,
        val cropWidth: Int,
        val cropHeight: Int,
        val scaleWidth: Int,
        val scaleHeight: Int,
        val timestampNs: Long,
        val drop: Boolean,
    )
}

expect fun applyFrameAdaptationParameters(
    frame: VideoFrame,
    parameters: VideoProcessor.FrameAdaptationParameters
): VideoFrame?
