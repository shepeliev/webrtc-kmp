package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.externals.RTCRtpReceiver

public actual class RtpReceiver internal constructor(
    internal val platform: RTCRtpReceiver,
) {
    public actual val id: String get() = platform.track.id
    public actual val track: MediaStreamTrack? get() = MediaStreamTrackImpl(platform.track)
    public actual val parameters: RtpParameters get() = RtpParameters(platform.getParameters())
}
