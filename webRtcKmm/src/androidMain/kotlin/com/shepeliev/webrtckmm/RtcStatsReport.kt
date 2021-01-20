package com.shepeliev.webrtckmm

import org.webrtc.RTCStatsReport

actual class RtcStatsReport(val native: RTCStatsReport) {
    actual val timestampUs: Long = native.timestampUs.toLong()
    actual val stats: Map<String, RtcStats> = native.statsMap.mapValues { (_, v) -> RtcStats(v) }
    actual override fun toString(): String = native.toString()
}

internal fun RTCStatsReport.asCommon() = RtcStatsReport(this)
