package com.shepeliev.webrtckmp

expect class MediaStream {
    val id: String
    val tracks: List<MediaStreamTrack>
    val audioTracks: List<AudioStreamTrack>
    val videoTracks: List<VideoStreamTrack>

    fun addTrack(track: AudioStreamTrack)
    fun addTrack(track: VideoStreamTrack)
    fun getTrackById(id: String): MediaStreamTrack?
    fun removeTrack(track: AudioStreamTrack)
    fun removeTrack(track: VideoStreamTrack)
    fun release()
}
