package com.shepeliev.webrtckmp

import org.webrtc.VideoTrack

internal class LocalVideoStreamTrack(
    android: VideoTrack,
    private val videoCaptureController: VideoCaptureController,
    override val constraints: MediaTrackConstraints,
) : RenderedVideoStreamTrack(android), VideoStreamTrack {

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
        videoCaptureController.stopCapture()
        videoCaptureController.dispose()
    }
}
