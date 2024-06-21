package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.RTCRtpReceiver

actual class RtpReceiver(val platform: RTCRtpReceiver) {
    actual val id: String get() = platform.track.id
    actual val track: MediaStreamTrack? get() = MediaStreamTrackImpl(platform.track)
    actual val parameters: RtpParameters get() = RtpParameters(platform.getParameters())
}
