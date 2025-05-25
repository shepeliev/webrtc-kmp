package com.shepeliev.webrtckmp

public expect class MediaStream() {
    public val id: String
    public val tracks: List<MediaStreamTrack>

    public fun addTrack(track: MediaStreamTrack)

    public fun getTrackById(id: String): MediaStreamTrack?

    public fun removeTrack(track: MediaStreamTrack)

    public fun release()
}

public val MediaStream.audioTracks: List<AudioStreamTrack>
    get() = tracks.mapNotNull { it as? AudioStreamTrack }

public val MediaStream.videoTracks: List<VideoStreamTrack>
    get() = tracks.mapNotNull { it as? VideoStreamTrack }
