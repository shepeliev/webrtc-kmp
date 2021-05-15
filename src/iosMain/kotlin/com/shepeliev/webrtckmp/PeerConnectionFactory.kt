package com.shepeliev.webrtckmp

import WebRTC.RTCPeerConnectionFactory
import kotlin.native.concurrent.freeze

internal actual class PeerConnectionFactory(val native: RTCPeerConnectionFactory) {
    actual fun createVideoSource(
        isScreencast: Boolean,
        alignTimestamps: Boolean
    ): VideoSource {
        return VideoSource(native.videoSource().freeze())
    }

    actual fun createVideoTrack(id: String, videoSource: VideoSource): VideoStreamTrack {
        return VideoStreamTrack(
            native.videoTrackWithSource(videoSource.native, id).freeze(),
            remote = false
        )
    }

    actual fun createAudioSource(constraints: MediaConstraints): AudioSource {
        return AudioSource(native.audioSourceWithConstraints(constraints.native).freeze())
    }

    actual fun createAudioTrack(id: String, audioSource: AudioSource): AudioStreamTrack {
        return AudioStreamTrack(
            native.audioTrackWithSource(audioSource.native, id).freeze(),
            remote = false
        )
    }

    actual fun startAecDump(filePath: String, fileSizeLimitBytes: Int) {
        native.startAecDumpWithFilePath(filePath, fileSizeLimitBytes.toLong())
    }

    actual fun stopAecDump() = native.stopAecDump()
}
