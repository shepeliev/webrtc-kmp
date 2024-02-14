package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.logging.Logging
import dev.onvoid.webrtc.media.MediaStreamTrack
import dev.onvoid.webrtc.media.MediaStreamTrackMuteListener
import dev.onvoid.webrtc.media.video.VideoTrack

internal class RemoteVideoStreamTrack(
    native: VideoTrack,
) : RenderedVideoStreamTrack(native), VideoStreamTrack, MediaStreamTrackMuteListener {

    override suspend fun switchCamera(deviceId: String?) {
        Logging.error("switchCamera is not supported for remote tracks")
    }

    override fun onSetEnabled(enabled: Boolean) {
        if (enabled) {
            native.addTrackMuteListener(this)
        } else {
            native.removeTrackMuteListener(this)
        }
    }

    override fun onTrackMute(track: MediaStreamTrack, muted: Boolean) {
        if(track == native) {
            setMuted(muted)
        }
    }
}
