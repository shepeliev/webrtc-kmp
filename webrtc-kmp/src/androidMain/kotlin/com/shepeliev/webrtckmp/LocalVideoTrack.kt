package com.shepeliev.webrtckmp

import org.webrtc.VideoTrack as AndroidVideoTrack

internal class LocalVideoTrack(
    native: AndroidVideoTrack,
    private val videoCaptureController: VideoCaptureController,
) : RenderedVideoTrack(native), VideoTrack {
    override val settings: MediaTrackSettings get() = videoCaptureController.settings

    override var shouldReceive: Boolean?
        get() = (native as AndroidVideoTrack).shouldReceive()
        set(value) { (native as AndroidVideoTrack).setShouldReceive(checkNotNull(value)) }

    init {
        videoCaptureController.videoCapturerErrorListener = VideoCapturerErrorListener { stop() }
        videoCaptureController.startCapture()
    }

    override suspend fun switchCamera(deviceId: String?) {
        (videoCaptureController as? CameraVideoCaptureController)?.let { controller ->
            deviceId?.let { controller.switchCamera(it) } ?: controller.switchCamera()
        }
    }

    override fun onSetEnabled(enabled: Boolean) {
        if (enabled) {
            videoCaptureController.startCapture()
        } else {
            videoCaptureController.stopCapture()
        }
    }

    override fun onStop() {
        videoCaptureController.dispose()
    }
}
