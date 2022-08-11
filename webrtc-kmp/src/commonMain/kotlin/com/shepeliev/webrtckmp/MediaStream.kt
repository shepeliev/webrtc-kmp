package com.shepeliev.webrtckmp

expect class MediaStream {
    val id: String
    val tracks: List<MediaStreamTrack>

    fun addTrack(track: MediaStreamTrack)
    fun getTrackById(id: String): MediaStreamTrack?
    fun removeTrack(track: MediaStreamTrack)
    fun release()
}

val MediaStream.audioTracks: List<AudioStreamTrack>
    get() = tracks.mapNotNull { it as? AudioStreamTrack }

val MediaStream.videoTracks: List<VideoStreamTrack>
    get() = tracks.mapNotNull { it as? VideoStreamTrack }
