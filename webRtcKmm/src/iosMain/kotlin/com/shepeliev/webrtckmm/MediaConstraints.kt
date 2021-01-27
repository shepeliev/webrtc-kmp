package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCMediaConstraints

actual class MediaConstraints private constructor(val native: RTCMediaConstraints) {
    actual constructor(mandatory: Map<String, String>, optional: Map<String, String>) : this(
        RTCMediaConstraints(mandatory as Map<Any?, *>, optional as Map<Any?, *>)
    )
}
