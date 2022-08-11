package com.shepeliev.webrtckmp

import WebRTC.RTCVideoRendererProtocol
import WebRTC.RTCVideoTrack

actual class VideoStreamTrack internal constructor(
    ios: RTCVideoTrack,
    private val videoCaptureController: VideoCaptureController? = null,
) : MediaStreamTrack(ios) {

    init {
        videoCaptureController?.startCapture()
    }

    fun addRenderer(renderer: RTCVideoRendererProtocol) {
        (ios as RTCVideoTrack).addRenderer(renderer)
    }

    fun removeRenderer(renderer: RTCVideoRendererProtocol) {
        (ios as RTCVideoTrack).removeRenderer(renderer)
    }

    actual suspend fun switchCamera(deviceId: String?) {
        (videoCaptureController as? CameraVideoCaptureController)?.let { controller ->
            deviceId?.let { controller.switchCamera(deviceId) } ?: controller.switchCamera()
        }
    }

    override fun onSetEnabled(enabled: Boolean) {
        if (enabled) {
            videoCaptureController?.startCapture()
        } else {
            videoCaptureController?.stopCapture()
        }
    }

    override fun onStop() {
        videoCaptureController?.stopCapture()
    }
}
