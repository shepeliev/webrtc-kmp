package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCLegacyStatsReport

actual class RtcStatsReport(val native: RTCLegacyStatsReport) {
    actual val timestampUs: Long = (native.timestamp * 1_000_000).toLong()
    actual val stats: Map<String, RtcStats> = buildStats()
    actual override fun toString(): String = native.toString()

    private fun buildStats(): Map<String, RtcStats> {
        val rtcStats = RtcStats(
            timestampUs = timestampUs,
            type = native.type,
            id = native.reportId,
            members =  native.values
                .filterValues { it != null }
                .map { (k, v) -> "$k" to v!! }.toMap()
        )

        return mapOf(native.reportId to rtcStats)
    }
}
