package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.util.UUID

interface MediaStream {
    val id: String
    val tracks: List<MediaStreamTrack>

    fun addTrack(track: MediaStreamTrack)
    fun getTrackById(id: String): MediaStreamTrack?
    fun removeTrack(track: MediaStreamTrack)

    @Deprecated("Use MediaStreamTrack.stop()", ReplaceWith("tracks.forEach(MediaStreamTrack::stop)"))
    fun release() {
        tracks.forEach(MediaStreamTrack::stop)
    }
}

expect fun MediaStream(tracks: List<MediaStreamTrack> = emptyList(), id: String = UUID.randomUUID()): MediaStream

fun MediaStream(stream: MediaStream): MediaStream = MediaStream(stream.tracks)

val MediaStream.audioTracks: List<AudioTrack>
    get() = tracks.mapNotNull { it as? AudioTrack }

val MediaStream.videoTracks: List<VideoTrack>
    get() = tracks.mapNotNull { it as? VideoTrack }

internal class CommonMediaStream(
    tracks: List<MediaStreamTrack> = emptyList(),
    override val id: String
) : MediaStream {
    override val tracks: List<MediaStreamTrack> get() = tracksMap.values.toList()

    private val tracksMap = tracks.associateBy { it.id }.toMutableMap()

    override fun addTrack(track: MediaStreamTrack) {
        tracksMap += track.id to track
    }

    override fun getTrackById(id: String): MediaStreamTrack? = tracksMap[id]

    override fun removeTrack(track: MediaStreamTrack) {
        tracksMap -= track.id
    }

    override fun toString(): String = "[$id:A=${audioTracks.size}:V=${videoTracks.size}]"
}
