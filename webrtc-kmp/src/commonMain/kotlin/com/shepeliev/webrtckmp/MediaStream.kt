package com.shepeliev.webrtckmp

public expect class MediaStream() {
    public val id: String
    public val tracks: List<MediaStreamTrack>

    public fun addTrack(track: MediaStreamTrack)

    public fun getTrackById(id: String): MediaStreamTrack?

    public fun removeTrack(track: MediaStreamTrack)

    public fun release()
}

public val MediaStream.audioTracks: List<AudioTrack> get() = tracks.mapNotNull { it as? AudioTrack }
public val MediaStream.videoTracks: List<VideoTrack> get() = tracks.mapNotNull { it as? VideoTrack }
