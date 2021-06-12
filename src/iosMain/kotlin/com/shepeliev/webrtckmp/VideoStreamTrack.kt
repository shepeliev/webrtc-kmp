package com.shepeliev.webrtckmp

import WebRTC.RTCVideoRendererProtocol
import WebRTC.RTCVideoTrack
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

actual class VideoStreamTrack internal constructor(
    ios: RTCVideoTrack,
    private val videoCaptureController: CameraVideoCaptureController? = null
) : MediaStreamTrack(ios) {

    init {
        onMute.onEach {
            videoCaptureController?.stopCapture()
        }.launchIn(scope)

        onUnmute.onEach {
            videoCaptureController?.initialize(ios.source)
            videoCaptureController?.startCapture()
        }.launchIn(scope)
    }

    fun addRenderer(renderer: RTCVideoRendererProtocol) {
        (ios as RTCVideoTrack).addRenderer(renderer)
    }

    fun removeRenderer(renderer: RTCVideoRendererProtocol) {
        (ios as RTCVideoTrack).removeRenderer(renderer)
    }

    actual suspend fun switchCamera() {
        videoCaptureController?.switchCamera()
    }

    actual suspend fun switchCamera(deviceId: String) {
        videoCaptureController?.switchCamera(deviceId)
    }

    actual override fun stop() {
        videoCaptureController?.stopCapture()
        super.stop()
    }
}
