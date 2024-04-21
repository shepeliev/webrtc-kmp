@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCVideoTrack
import kotlinx.cinterop.ExperimentalForeignApi

internal class LocalVideoStreamTrack(
    ios: RTCVideoTrack,
    private val videoCaptureController: VideoCaptureController,
) : RenderedVideoStreamTrack(ios), VideoStreamTrack {
    override val settings: MediaTrackSettings get() = videoCaptureController.settings

    init {
        videoCaptureController.startCapture()
    }

    override suspend fun switchCamera(deviceId: String?) {
        (videoCaptureController as? CameraVideoCaptureController)?.let { controller ->
            deviceId?.let { controller.switchCamera(deviceId) } ?: controller.switchCamera()
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
    }
}
