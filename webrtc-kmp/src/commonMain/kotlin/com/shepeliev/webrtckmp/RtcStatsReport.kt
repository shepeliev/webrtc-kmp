package com.shepeliev.webrtckmp

public expect class RtcStatsReport {
    public val timestampUs: Long
    public val stats: Map<String, RtcStats>

    override fun toString(): String
}
