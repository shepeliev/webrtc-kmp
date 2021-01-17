package com.shepeliev.webrtckmm

expect class MediaStream {
    val id: String

    fun addTrack(track: AudioTrack): Boolean
    fun addTrack(track: VideoTrack): Boolean
    fun addPreservedTrack(track: VideoTrack): Boolean
    fun removeTrack(track: AudioTrack): Boolean
    fun removeTrack(track: VideoTrack): Boolean
    override fun toString(): String
}
