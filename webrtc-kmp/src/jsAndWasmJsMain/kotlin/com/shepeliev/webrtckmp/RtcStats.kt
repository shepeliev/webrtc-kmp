package com.shepeliev.webrtckmp

public actual class RtcStats {
    public actual val timestampUs: Long = -1
    public actual val type: String = "not_implemented"
    public actual val id: String = "not_implemented"
    public actual val members: Map<String, Any> = emptyMap()

    actual override fun toString(): String = "not_implemented"
}
