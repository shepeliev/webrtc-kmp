package com.shepeliev.webrtckmp.sample.shared

import com.arkivanov.decompose.value.Value
import com.shepeliev.webrtckmp.MediaStream

interface Room {

    val model: Value<Model>

    fun openUserMedia()
    fun switchCamera()
    fun createRoom()
    fun joinRoom(roomId: String)
    fun hangup()

    data class Model(
        val localStream: MediaStream? = null,
        val remoteStream: MediaStream? = null,
        val roomId: String? = null,
        val isCaller: Boolean? = null,
        val isJoining: Boolean = false,
    )
}
