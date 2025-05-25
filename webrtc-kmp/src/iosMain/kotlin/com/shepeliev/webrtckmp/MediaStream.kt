@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack
import WebRTC.RTCMediaStream
import WebRTC.RTCVideoTrack
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSUUID

public actual class MediaStream internal constructor(
    public val ios: RTCMediaStream,
) {
    public actual constructor() : this(
        WebRtc.peerConnectionFactory.mediaStreamWithStreamId(NSUUID.UUID().UUIDString),
    )

    public actual val id: String = ios.streamId
    private val _tracks = mutableListOf<MediaStreamTrack>()
    public actual val tracks: List<MediaStreamTrack> = _tracks

    public actual fun addTrack(track: MediaStreamTrack) {
        require(track is MediaStreamTrackImpl)

        ios.let {
            when (track.ios) {
                is RTCAudioTrack -> it.addAudioTrack(track.ios)
                is RTCVideoTrack -> it.addVideoTrack(track.ios)
                else -> error("Unknown MediaStreamTrack kind: ${track.kind}")
            }
        }
        _tracks += track
    }

    public actual fun getTrackById(id: String): MediaStreamTrack? =
        tracks.firstOrNull { it.id == id }

    public actual fun removeTrack(track: MediaStreamTrack) {
        require(track is MediaStreamTrackImpl)

        when (track.ios) {
            is RTCAudioTrack -> ios.removeAudioTrack(track.ios)
            is RTCVideoTrack -> ios.removeVideoTrack(track.ios)
            else -> error("Unknown MediaStreamTrack kind: ${track.kind}")
        }
        _tracks -= track
    }

    public actual fun release() {
        tracks.forEach(MediaStreamTrack::stop)
    }
}
