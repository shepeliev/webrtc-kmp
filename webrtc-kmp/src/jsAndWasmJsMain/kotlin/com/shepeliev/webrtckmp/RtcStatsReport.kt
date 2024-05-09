package com.shepeliev.webrtckmp

actual class RtcStatsReport {
    actual val timestampUs: Long = -1
    actual val stats: Map<String, RtcStats> = emptyMap()

    actual override fun toString(): String = "not_implemented"
}
