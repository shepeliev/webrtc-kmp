package com.shepeliev.webrtckmm

import org.webrtc.VideoSink
import org.webrtc.CapturerObserver as NativeCapturerObserver
import org.webrtc.VideoSource as NativeVideoSource
import org.webrtc.VideoProcessor as NativeVideoProcessor
import org.webrtc.VideoFrame as NativeVideoFrame

actual class VideoSource internal constructor(override val native: NativeVideoSource) :
    BaseMediaSource(), MediaSource {

    actual override val state: MediaSource.State
        get() = super.state

    actual val capturerObserver: CapturerObserver
        get() = NativeCapturerObserverAdapter(native.capturerObserver)

    val nativeCapturerObserver: NativeCapturerObserver
        get() = native.capturerObserver

    actual fun setIsScreencast(isScreencast: Boolean) {
        native.setIsScreencast(isScreencast)
    }

    actual fun adaptOutputFormat(width: Int, height: Int, fps: Int) {
        native.adaptOutputFormat(width, height, fps)
    }

    actual fun adaptOutputFormat(
        landscapeWidth: Int,
        landscapeHeight: Int,
        portraitWidth: Int,
        portraitHeight: Int,
        fps: Int
    ) {
        native.adaptOutputFormat(
            landscapeWidth,
            landscapeHeight,
            portraitWidth,
            portraitHeight,
            fps
        )
    }

    actual fun adaptOutputFormat(
        targetLandscapeAspectRatio: AspectRatio,
        targetPortraitAspectRatio: AspectRatio,
        maxLandscapePixelCount: Int?,
        maxPortraitPixelCount: Int?,
        maxFps: Int?
    ) {
    }

    actual fun setVideoProcessor(videoProcessor: VideoProcessor?) {
        native.setVideoProcessor(videoProcessor?.let { CommonVideoProcessorAdapter(it) })
    }

    actual override fun dispose() {
        super.dispose()
    }
}

private inline class NativeCapturerObserverAdapter(val native: NativeCapturerObserver) : CapturerObserver {
    override fun onCapturerStarted(success: Boolean) {
        native.onCapturerStarted(success)
    }

    override fun onCapturerStopped() {
        native.onCapturerStopped()
    }

    override fun onFrameCaptured(frame: VideoFrame) {
        native.onFrameCaptured(frame.native)
    }
}

private class CommonVideoProcessorAdapter(val videoProcessor: VideoProcessor): NativeVideoProcessor {
    override fun onCapturerStarted(success: Boolean) {
        videoProcessor.onCapturerStarted(success)
    }

    override fun onCapturerStopped() {
        videoProcessor.onCapturerStopped()
    }

    override fun onFrameCaptured(frame: NativeVideoFrame) {
        videoProcessor.onFrameCaptured(VideoFrame(frame))
    }

    override fun setSink(sink: VideoSink?) {
        videoProcessor.setSink(sink?.let { NativeVideoSinkAdapter(it) })
    }
}
