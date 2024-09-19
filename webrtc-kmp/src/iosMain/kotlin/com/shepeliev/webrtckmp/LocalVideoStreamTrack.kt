@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCVideoTrack
import com.shepeliev.webrtckmp.capturer.CameraVideoCapturerController
import com.shepeliev.webrtckmp.capturer.VideoCapturerController
import kotlinx.cinterop.ExperimentalForeignApi

internal class LocalVideoStreamTrack(
    ios: RTCVideoTrack,
    private val videoCapturerController: VideoCapturerController,
) : RenderedVideoStreamTrack(ios), VideoStreamTrack {
    override val settings: MediaTrackSettings get() = videoCapturerController.settings

    init {
        videoCapturerController.startCapture()
    }

    override suspend fun switchCamera(deviceId: String?) {
        (videoCapturerController as? CameraVideoCapturerController)?.let { controller ->
            deviceId?.let { controller.switchCamera(deviceId) } ?: controller.switchCamera()
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
        videoCapturerController.stopCapture()
    }
}
