package com.shepeliev.webrtckmp

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

    override suspend fun switchCamera(deviceId: String?) {}

    override fun onSetEnabled(enabled: Boolean) {
        native.isEnabled = enabled
    }

    override fun onStop() {
        videoSource.stop()
        videoSource.dispose()
    }
}
