package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCVideoCapturerDelegateProtocol
import cocoapods.GoogleWebRTC.RTCVideoSource

actual class VideoSource internal constructor(override val native: RTCVideoSource) :
    BaseMediaSource(), MediaSource {

    actual override val state: MediaSource.State
        get() = super.state

    val nativeCapturerObserver: RTCVideoCapturerDelegateProtocol
        get() = native

    actual fun setIsScreencast(isScreencast: Boolean) {
        // not applicable
    }

    actual fun adaptOutputFormat(width: Int, height: Int, fps: Int) {
        native.adaptOutputFormatToWidth(width, height, fps)
    }
}
