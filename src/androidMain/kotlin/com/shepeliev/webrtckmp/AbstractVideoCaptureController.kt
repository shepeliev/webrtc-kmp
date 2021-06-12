package com.shepeliev.webrtckmp

import org.webrtc.CapturerObserver
import org.webrtc.Size
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer

abstract class AbstractVideoCaptureController {
    val isScreencast: Boolean
        get() = videoCapturer.isScreencast

    protected val videoCapturer: VideoCapturer by lazy { createVideoCapturer() }

    private var textureHelper: SurfaceTextureHelper? = null

    abstract fun createVideoCapturer(): VideoCapturer

    abstract fun selectVideoSize(): Size

    abstract fun selectFps(): Int

    fun initialize(observer: CapturerObserver) {
        textureHelper = SurfaceTextureHelper.create("VideoCapturerTextureHelper", eglBaseContext)
        videoCapturer.initialize(textureHelper, applicationContext, observer)
    }

    fun startCapture() {
        val size = selectVideoSize()
        val fps = selectFps()
        videoCapturer.startCapture(size.width, size.height, fps)
    }

    fun stopCapture() {
        videoCapturer.stopCapture()
        textureHelper?.dispose()
        textureHelper = null
    }
}
