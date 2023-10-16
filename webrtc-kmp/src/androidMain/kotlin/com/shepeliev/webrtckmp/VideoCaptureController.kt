package com.shepeliev.webrtckmp

import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource

@Suppress("unused")
internal abstract class VideoCaptureController(
    private val videoSource: VideoSource,
    settings: MediaTrackSettings,
) {
    val isScreencast: Boolean
        get() = videoCapturer.isScreencast

    var settings: MediaTrackSettings = settings
        protected set

    var videoCapturerStopListener: VideoCapturerStopListener = VideoCapturerStopListener { }

    protected val videoCapturer: VideoCapturer by lazy { createVideoCapturer() }
    private var textureHelper: SurfaceTextureHelper? = null
    private var disposed = false

    abstract fun createVideoCapturer(): VideoCapturer

    fun startCapture() {
        check(!disposed) { "Video capturer disposed" }
        check(textureHelper == null) { "Video capturer already started" }
        textureHelper = SurfaceTextureHelper.create("VideoCapturerTextureHelper", WebRtc.rootEglBase.eglBaseContext)
        videoCapturer.initialize(textureHelper, ApplicationContextHolder.context, videoSource.capturerObserver)
        videoCapturer.startCapture(settings.width!!, settings.height!!, settings.frameRate!!.toInt())
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

fun interface VideoCapturerStopListener {
    fun onStop(reason: String)
}
