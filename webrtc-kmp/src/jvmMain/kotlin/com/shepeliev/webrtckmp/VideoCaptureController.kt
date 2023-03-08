package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.video.VideoCapture
import dev.onvoid.webrtc.media.video.VideoCaptureCapability

internal abstract class VideoCaptureController {
    protected val videoCapturer: VideoCapture by lazy { createVideoCapture() }

    private var disposed = false

    abstract fun createVideoCapture(): VideoCapture

    abstract fun selectVideoSize(): Size

    abstract fun selectFps(): Int

    fun startCapture() {
        check(!disposed) { "Video capturer disposed" }

        val size = selectVideoSize()
        val fps = selectFps()

        with(videoCapturer) {
            setVideoCaptureCapability(VideoCaptureCapability(size.width, size.height, fps))
            start()
        }
    }

    fun stopCapture() {
        check(!disposed) { "Video capturer disposed" }
        videoCapturer.stop()
    }

    fun dispose() {
        stopCapture()
        videoCapturer.dispose()
        disposed = true
    }
}
