package com.shepeliev.webrtckmp

expect class MediaStream : VideoStream {
    val id: String
    val audioTracks: List<AudioTrack>
    val videoTracks: List<VideoTrack>

    fun addTrack(audioTrack: AudioTrack): Boolean
    fun addTrack(videoTrack: VideoTrack): Boolean
    fun removeTrack(audioTrack: AudioTrack): Boolean
    fun removeTrack(videoTrack: VideoTrack): Boolean

    override fun videoTrack(): VideoTrack?
}

fun interface VideoStream {
    fun videoTrack(): VideoTrack?
}
