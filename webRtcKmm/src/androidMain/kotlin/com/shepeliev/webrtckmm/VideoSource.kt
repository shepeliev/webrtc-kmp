package com.shepeliev.webrtckmm

import org.webrtc.CapturerObserver as NativeCapturerObserver
import org.webrtc.VideoSource as NativeVideoSource

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

    actual override fun dispose() {
        super.dispose()
    }
}

private inline class NativeCapturerObserverAdapter(val native: NativeCapturerObserver) :
    CapturerObserver {
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
