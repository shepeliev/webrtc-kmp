@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCRtpReceiver

actual class RtpReceiver(val native: RTCRtpReceiver, actual val track: MediaStreamTrack?) {
    actual val id: String
        get() = native.receiverId

    actual val parameters: RtpParameters
        get() = RtpParameters(native.parameters)
}
