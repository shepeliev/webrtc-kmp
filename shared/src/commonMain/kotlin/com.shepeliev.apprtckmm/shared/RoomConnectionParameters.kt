package com.shepeliev.apprtckmm.shared

/**
 * Struct holding the connection parameters of an AppRTC room.
 */
data class RoomConnectionParameters(
    val roomUrl: String,
    val roomId: String,
    val loopback: Boolean,
    val urlParameters: String? = null /* urlParameters */
)
