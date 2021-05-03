package com.shepeliev.webrtckmp

actual data class RtcStats internal constructor(
    actual val timestampUs: Long,
    actual val type: String,
    actual val id: String,
    actual val members: Map<String, Any>,
) {
    actual override fun toString(): String = super.toString()
}
