@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCStatistics
import WebRTC.RTCStatisticsReport
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual class RtcStatsReport(val native: RTCStatisticsReport) {
    actual val timestampUs: Long = (native.timestamp_us).toLong()
    actual val stats: Map<String, RtcStats> = native.statistics
        .map { (k, v) -> "$k" to RtcStats(v as RTCStatistics) }
        .toMap()

    actual override fun toString(): String = native.toString()
}
