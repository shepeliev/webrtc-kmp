@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCRtpReceiver
import kotlinx.cinterop.ExperimentalForeignApi

public actual class RtpReceiver(
    public val native: RTCRtpReceiver,
    public actual val track: MediaStreamTrack?,
) {
    public actual val id: String get() = native.receiverId
    public actual val parameters: RtpParameters get() = RtpParameters(native.parameters)
}
