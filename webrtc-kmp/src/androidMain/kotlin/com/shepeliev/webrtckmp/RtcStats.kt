package com.shepeliev.webrtckmp

import org.webrtc.RTCStats

public actual class RtcStats internal constructor(
    public val native: RTCStats,
) {
    public actual val timestampUs: Long = native.timestampUs.toLong()
    public actual val type: String = native.type
    public actual val id: String = native.id
    public actual val members: Map<String, Any> = native.members

    actual override fun toString(): String = native.toString()
}
