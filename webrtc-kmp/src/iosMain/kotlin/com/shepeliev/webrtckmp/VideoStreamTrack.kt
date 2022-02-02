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

        videoCaptureController?.initialize(ios.source)
        videoCaptureController?.startCapture()
    }

    fun addRenderer(renderer: RTCVideoRendererProtocol) {
        (ios as RTCVideoTrack).addRenderer(renderer)
    }

    fun removeRenderer(renderer: RTCVideoRendererProtocol) {
        (ios as RTCVideoTrack).removeRenderer(renderer)
    }

    actual suspend fun switchCamera(deviceId: String?) {
        if (deviceId == null) {
            videoCaptureController?.switchCamera()
        } else {
            videoCaptureController?.switchCamera(deviceId)
        }
    }

    override fun stop() {
        videoCaptureController?.stopCapture()
        super.stop()
    }
}
