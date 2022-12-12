package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCStatsReport

actual class RtcStatsReport(val native: RTCStatsReport) {
    actual val timestampUs: Long = native.stats.values.firstOrNull()?.timestamp ?: -1
    actual val stats: Map<String, RtcStats> = native.stats.mapValues { (_, v) -> RtcStats(v) }
    actual override fun toString(): String = native.toString()
}
