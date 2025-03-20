package com.shepeliev.webrtckmp

import WebRTC.RTCStatistics
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual data class RtcStats internal constructor(val native: RTCStatistics) {
    actual val timestampUs: Long = native.timestamp_us.toLong()
    actual val type: String = native.type
    actual val id: String = native.id
    actual val members: Map<String, Any> = native.values
        .filterValues { it != null }
        .map { (k, v) -> "$k" to v!! }.toMap()

    actual override fun toString(): String = super.toString()
}
