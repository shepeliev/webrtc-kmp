package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack
import WebRTC.RTCMediaStream
import WebRTC.RTCVideoTrack
import platform.Foundation.NSUUID

actual class MediaStream internal constructor(
    val ios: RTCMediaStream?,
    actual val id: String = ios?.streamId ?: NSUUID.UUID().UUIDString
) {
    actual val tracks: List<MediaStreamTrack>
        get() = audioTracks + videoTracks

    private var audioTracksInternal = mutableListOf<AudioStreamTrack>()
    actual val audioTracks: List<AudioStreamTrack> = audioTracksInternal

    private var videoTracksInternal = mutableListOf<VideoStreamTrack>()
    actual val videoTracks: List<VideoStreamTrack> = videoTracksInternal

    actual fun addTrack(track: AudioStreamTrack) {
        ios?.addAudioTrack(track.ios as RTCAudioTrack)
        audioTracksInternal += track
    }

    actual fun addTrack(track: VideoStreamTrack) {
        ios?.addVideoTrack(track.ios as RTCVideoTrack)
        videoTracksInternal += track
    }

    actual fun getTrackById(id: String): MediaStreamTrack? {
        return tracks.firstOrNull { it.id == id }
    }

    actual fun removeTrack(track: AudioStreamTrack) {
        ios?.removeAudioTrack(track.ios as RTCAudioTrack)
        audioTracksInternal -= track
    }

    actual fun removeTrack(track: VideoStreamTrack) {
        ios?.removeVideoTrack(track.ios as RTCVideoTrack)
        videoTracksInternal -= track
    }

    actual fun release() {
        tracks.forEach(MediaStreamTrack::stop)
    }
}
