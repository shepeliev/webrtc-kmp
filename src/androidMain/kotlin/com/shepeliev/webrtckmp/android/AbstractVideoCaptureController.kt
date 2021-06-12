package com.shepeliev.webrtckmp.android

import com.shepeliev.webrtckmp.applicationContext
import com.shepeliev.webrtckmp.eglBaseContext
import org.webrtc.CapturerObserver
import org.webrtc.Size
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer

abstract class AbstractVideoCaptureController {
    val isScreencast: Boolean
        get() = videoCapturer.isScreencast

    protected val videoCapturer: VideoCapturer by lazy { createVideoCapturer() }

    abstract fun createVideoCapturer(): VideoCapturer

    abstract fun selectVideoSize(): Size

    abstract fun selectFps(): Int

    fun initialize(observer: CapturerObserver) {
        val helper = SurfaceTextureHelper.create("VideoCapturerTextureHelper", eglBaseContext)
        videoCapturer.initialize(helper, applicationContext, observer)
    }

    fun startCapture() {
        val size = selectVideoSize()
        val fps = selectFps()
        videoCapturer.startCapture(size.width, size.height, fps)
    }

    fun stopCapture() {
        videoCapturer.stopCapture()
    }
}
