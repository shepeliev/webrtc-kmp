package com.shepeliev.webrtckmp

public expect class RtcStats {
    public val timestampUs: Long
    public val type: String
    public val id: String
    public val members: Map<String, Any>

    override fun toString(): String
}
