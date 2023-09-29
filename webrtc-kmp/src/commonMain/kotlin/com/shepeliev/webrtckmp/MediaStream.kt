package com.shepeliev.webrtckmp

expect class MediaStream {
    val id: String
    val tracks: List<MediaStreamTrack>

    fun addTrack(track: MediaStreamTrack)
    fun getTrackById(id: String): MediaStreamTrack?
    fun removeTrack(track: MediaStreamTrack)
    fun release()
}

val MediaStream.audioTracks: List<AudioTrack>
    get() = tracks.mapNotNull { it as? AudioTrack }

val MediaStream.videoTracks: List<VideoTrack>
    get() = tracks.mapNotNull { it as? VideoTrack }
