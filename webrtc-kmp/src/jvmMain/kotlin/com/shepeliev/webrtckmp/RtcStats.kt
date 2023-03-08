package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCStats

actual class RtcStats(val native: RTCStats) {
    actual val timestampUs: Long
        get() = native.timestamp

    actual val type: String
        get() = native.type.name

    actual val id: String
        get() = native.id

    actual val members: Map<String, Any>
        get() = native.members

    actual override fun toString(): String {
        return "RtcStats(timestampUs=$timestampUs, type=$type, id=$id, members=$members)"
    }
}
