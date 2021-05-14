package com.shepeliev.webrtckmp

import WebRTC.RTCVideoCapturerDelegateProtocol
import WebRTC.RTCVideoSource

actual class VideoSource internal constructor(override val native: RTCVideoSource) :
    BaseMediaSource(), MediaSource {

    actual override val state: MediaSource.State
        get() = super.state

    val nativeCapturerObserver: RTCVideoCapturerDelegateProtocol
        get() = native

    actual fun adaptOutputFormat(width: Int, height: Int, fps: Int) {
        native.adaptOutputFormatToWidth(width, height, fps)
    }

    actual fun dispose() {
        // not implemented in iOS
    }
}
