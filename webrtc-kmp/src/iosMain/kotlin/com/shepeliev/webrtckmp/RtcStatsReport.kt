@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCStatistics
import WebRTC.RTCStatisticsReport
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
public actual class RtcStatsReport(
    public val native: RTCStatisticsReport,
) {
    public actual val timestampUs: Long = (native.timestamp_us).toLong()
    public actual val stats: Map<String, RtcStats> =
        native.statistics
            .map { (k, v) -> "$k" to RtcStats(v as RTCStatistics) }
            .toMap()

    actual override fun toString(): String = native.toString()
}
