package com.shepeliev.webrtckmp

import org.webrtc.VideoSink
import org.webrtc.VideoTrack

actual class VideoStreamTrack internal constructor(
    android: VideoTrack,
    private val videoCaptureController: VideoCaptureController? = null,
) : MediaStreamTrack(android) {

    init {
        videoCaptureController?.videoCapturerErrorListener = VideoCapturerErrorListener { stop() }
        videoCaptureController?.startCapture()
    }

    actual suspend fun switchCamera(deviceId: String?) {
        (videoCaptureController as? CameraVideoCaptureController)?.let { controller ->
            deviceId?.let { controller.switchCamera(it) } ?: controller.switchCamera()
        }
    }

    fun addSink(sink: VideoSink) {
        (android as VideoTrack).addSink(sink)
    }

    fun removeSink(sink: VideoSink) {
        (android as VideoTrack).removeSink(sink)
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
        videoCaptureController?.dispose()
    }
}
