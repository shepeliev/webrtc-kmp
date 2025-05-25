package com.shepeliev.webrtckmp

import org.webrtc.RtpReceiver as NativeRtpReceiver

public actual class RtpReceiver(
    public val native: NativeRtpReceiver,
    public actual val track: MediaStreamTrack?,
) {
    public actual val id: String get() = native.id()
    public actual val parameters: RtpParameters get() = RtpParameters(native.parameters)
}
