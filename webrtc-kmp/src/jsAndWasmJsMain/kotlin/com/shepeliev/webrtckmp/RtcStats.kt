package com.shepeliev.webrtckmp

actual class RtcStats {
    actual val timestampUs: Long = -1
    actual val type: String = "not_implemented"
    actual val id: String = "not_implemented"
    actual val members: Map <String, Any> = emptyMap()

    actual override fun toString(): String = "not_implemented"
}
