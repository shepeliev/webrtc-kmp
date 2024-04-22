package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.MediaDevices
import dev.onvoid.webrtc.media.video.VideoDeviceSource
import dev.onvoid.webrtc.media.video.VideoTrack

internal class LocalVideoStreamTrack(
    native: VideoTrack,
    private val videoSource: VideoDeviceSource,
    override val settings: MediaTrackSettings,
) : RenderedVideoStreamTrack(native), VideoStreamTrack {

    init {
        videoSource.start()
    }

    override suspend fun switchCamera(deviceId: String?) {
        if(native.id != deviceId) {
            videoSource.stop()
            // if the deviceId is null, no new camera will be set, effectively "muting" video
            deviceId?.let { id ->
                MediaDevices.getVideoCaptureDevices().firstOrNull { it.descriptor == id }
                    ?.let { device ->
                        videoSource.setVideoCaptureDevice(device)
                        videoSource.start()
                    }
            }
        }
    }

    override fun onSetEnabled(enabled: Boolean) {
        native.isEnabled = enabled
    }

    override fun onStop() {
        videoSource.stop()
        videoSource.dispose()
    }
}
