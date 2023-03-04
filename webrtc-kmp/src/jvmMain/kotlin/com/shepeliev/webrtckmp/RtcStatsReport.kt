package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCStatsReport

actual class RtcStatsReport(val native: RTCStatsReport) {
    actual val timestampUs: Long
        get() = 0 // TODO
    actual val stats: Map<String, RtcStats>
        get() = native.stats.mapValues { RtcStats(it.value) }

    actual override fun toString(): String {
        return "RtcStatsReport(timestampUs=$timestampUs, stats=[${stats.toList().joinToString()}])"
    }
}
