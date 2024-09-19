package com.shepeliev.webrtckmp

import org.webrtc.Size
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource

internal abstract class VideoCapturerController(private val videoSource: VideoSource) {
    val isScreencast: Boolean
        get() = videoCapturer.isScreencast

    var settings: MediaTrackSettings = MediaTrackSettings()
        protected set

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
        textureHelper = SurfaceTextureHelper.create("VideoCapturerTextureHelper", WebRtc.rootEglBase.eglBaseContext)
        videoCapturer.initialize(textureHelper, WebRtc.applicationContext, videoSource.capturerObserver)
        val size = selectVideoSize()
        val fps = selectFps()
        settings = settings.copy(
            width = size.width,
            height = size.height,
            frameRate = fps.toDouble()
        )
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
