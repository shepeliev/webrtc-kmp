package com.shepeliev.webrtckmm

import org.webrtc.RTCStats

actual class RtcStats internal constructor(val native: RTCStats) {
    actual val timestampUs: Long = native.timestampUs.toLong()
    actual val type: String = native.type
    actual val id: String = native.id
    actual val members: Map<String, Any> = native.members
    actual override fun toString():String = native.toString()
}
