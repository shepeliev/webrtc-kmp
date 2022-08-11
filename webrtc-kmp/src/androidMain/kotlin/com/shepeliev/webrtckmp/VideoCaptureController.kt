package com.shepeliev.webrtckmp

import org.webrtc.Size
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource

internal abstract class VideoCaptureController(private val videoSource: VideoSource) {
    val isScreencast: Boolean
        get() = videoCapturer.isScreencast

    var videoCapturerErrorListener: VideoCapturerErrorListener = VideoCapturerErrorListener { }

    protected val videoCapturer: VideoCapturer by lazy { createVideoCapturer() }
    private var textureHelper: SurfaceTextureHelper? = null
    private var disposed = false

    abstract fun createVideoCapturer(): VideoCapturer

    abstract fun selectVideoSize(): Size

    abstract fun selectFps(): Int

    fun startCapture() {
        check(!disposed) { "Video capturer disposed" }
        check(textureHelper == null) { "Video capturer already started" }
        textureHelper = SurfaceTextureHelper.create("VideoCapturerTextureHelper", eglBaseContext)
        videoCapturer.initialize(textureHelper, applicationContext, videoSource.capturerObserver)
        val size = selectVideoSize()
        val fps = selectFps()
        videoCapturer.startCapture(size.width, size.height, fps)
    }

    fun stopCapture() {
        check(!disposed) { "Video capturer disposed" }
        if (textureHelper == null) return
        videoCapturer.stopCapture()
        textureHelper?.stopListening()
        textureHelper?.dispose()
        textureHelper = null
    }

    fun dispose() {
        stopCapture()
        videoCapturer.dispose()
        videoSource.dispose()
    }
}

fun interface VideoCapturerErrorListener {
    fun onError(error: String)
}
