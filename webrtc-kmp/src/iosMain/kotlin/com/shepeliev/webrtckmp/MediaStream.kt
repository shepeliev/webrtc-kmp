@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack
import WebRTC.RTCMediaStream
import WebRTC.RTCVideoTrack
import platform.Foundation.NSUUID

actual class MediaStream internal constructor(
    val ios: RTCMediaStream?,
    actual val id: String = ios?.streamId ?: NSUUID.UUID().UUIDString,
) {

    private val _tracks = mutableListOf<MediaStreamTrack>()
    actual val tracks: List<MediaStreamTrack> = _tracks

    actual fun addTrack(track: MediaStreamTrack) {
        require(track is MediaStreamTrackImpl)

        ios?.let {
            when (track.ios) {
                is RTCAudioTrack -> it.addAudioTrack(track.ios)
                is RTCVideoTrack -> it.addVideoTrack(track.ios)
                else -> error("Unknown MediaStreamTrack kind: ${track.kind}")
            }
        }
        _tracks += track
    }

    actual fun getTrackById(id: String): MediaStreamTrack? {
        return tracks.firstOrNull { it.id == id }
    }

    actual fun removeTrack(track: MediaStreamTrack) {
        require(track is MediaStreamTrackImpl)

        ios?.let {
            when (track.ios) {
                is RTCAudioTrack -> it.removeAudioTrack(track.ios)
                is RTCVideoTrack -> it.removeVideoTrack(track.ios)
                else -> error("Unknown MediaStreamTrack kind: ${track.kind}")
            }
        }
        _tracks -= track
    }

    actual fun release() {
        tracks.forEach(MediaStreamTrack::stop)
    }
}
