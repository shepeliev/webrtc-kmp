package com.shepeliev.webrtckmp

import org.webrtc.RTCStatsReport

public actual class RtcStatsReport(
    public val native: RTCStatsReport,
) {
    public actual val timestampUs: Long = native.timestampUs.toLong()

    public actual val stats: Map<String, RtcStats> =
        native.statsMap.mapValues { (_, v) -> RtcStats(v) }

    actual override fun toString(): String = native.toString()
}
