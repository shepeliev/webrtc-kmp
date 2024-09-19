package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.capturer.CameraVideoCapturerController
import com.shepeliev.webrtckmp.capturer.VideoCapturerController
import com.shepeliev.webrtckmp.capturer.VideoCapturerErrorListener
import org.webrtc.VideoTrack

internal class LocalVideoStreamTrack(
    android: VideoTrack,
    private val videoCapturerController: VideoCapturerController,
) : RenderedVideoStreamTrack(android), VideoStreamTrack {
    override val settings: MediaTrackSettings get() = videoCapturerController.settings

    init {
        videoCapturerController.videoCapturerErrorListener = VideoCapturerErrorListener { stop() }
        videoCapturerController.startCapture()
    }

    override suspend fun switchCamera(deviceId: String?) {
        (videoCapturerController as? CameraVideoCapturerController)?.let { controller ->
            deviceId?.let { controller.switchCamera(it) } ?: controller.switchCamera()
        }
    }

    override fun onSetEnabled(enabled: Boolean) {
        if (enabled) {
            videoCapturerController.startCapture()
        } else {
            videoCapturerController.stopCapture()
        }
    }

    override fun onStop() {
        videoCapturerController.dispose()
    }
}
