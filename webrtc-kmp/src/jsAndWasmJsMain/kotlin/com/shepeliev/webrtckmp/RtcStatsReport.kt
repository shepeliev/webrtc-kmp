package com.shepeliev.webrtckmp

public actual class RtcStatsReport {
    public actual val timestampUs: Long = -1
    public actual val stats: Map<String, RtcStats> = emptyMap()

    actual override fun toString(): String = "not_implemented"
}
