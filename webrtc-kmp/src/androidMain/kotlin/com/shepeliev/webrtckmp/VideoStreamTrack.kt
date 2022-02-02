package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.webrtc.VideoSink
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

actual class VideoStreamTrack internal constructor(
    android: VideoTrack,
    videoSource: VideoSource? = null,
    private val videoCaptureController: AbstractVideoCaptureController? = null
) : MediaStreamTrack(android, videoSource) {

    init {
        onMute.onEach { videoCaptureController?.stopCapture() }.launchIn(scope)

        onUnmute.onEach {
            videoSource?.also {
                videoCaptureController?.initialize(it.capturerObserver)
                videoCaptureController?.startCapture()
            }
        }.launchIn(scope)
    }

    actual suspend fun switchCamera(deviceId: String?) {
        if (deviceId == null) {
            (videoCaptureController as? CameraVideoCaptureController)?.switchCamera()
        } else {
            (videoCaptureController as? CameraVideoCaptureController)?.switchCamera(deviceId)
        }
    }

    fun addSink(sink: VideoSink) {
        (android as VideoTrack).addSink(sink)
    }

    fun removeSink(sink: VideoSink) {
        (android as VideoTrack).removeSink(sink)
    }

    override fun stop() {
        videoCaptureController?.stopCapture()
        super.stop()
    }
}
