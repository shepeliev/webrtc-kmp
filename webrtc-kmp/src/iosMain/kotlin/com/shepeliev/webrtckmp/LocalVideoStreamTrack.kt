package com.shepeliev.webrtckmp

import WebRTC.RTCVideoTrack

internal class LocalVideoStreamTrack(
    ios: RTCVideoTrack,
    private val videoCaptureController: VideoCaptureController,
) : RenderedVideoStreamTrack(ios), VideoStreamTrack {
    override val settings: MediaTrackSettings get() = videoCaptureController.settings
    override var shouldReceive: Boolean?
        get() = (ios as RTCVideoTrack).shouldReceive
        set(value) { (ios as RTCVideoTrack).shouldReceive = checkNotNull(value) }

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
