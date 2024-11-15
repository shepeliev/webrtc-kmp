package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.video.VideoDesktopSource
import dev.onvoid.webrtc.media.video.VideoTrack

internal class DesktopVideoStreamTrack(
    native: VideoTrack,
    private val videoSource: VideoDesktopSource,
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
