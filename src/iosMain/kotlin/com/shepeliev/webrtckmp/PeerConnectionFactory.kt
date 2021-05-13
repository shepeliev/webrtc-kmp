package com.shepeliev.webrtckmp

import WebRTC.RTCPeerConnectionFactory
import kotlin.native.concurrent.freeze

internal actual class PeerConnectionFactory(val native: RTCPeerConnectionFactory) {
    actual fun createPeerConnection(
        rtcConfiguration: RtcConfiguration,
        constraints: MediaConstraints,
    ): PeerConnection {
        val pcEvents = PeerConnectionEvents().freeze()
        val nativePc = WebRtcKmp.peerConnectionFactory.native.peerConnectionWithConfiguration(
            rtcConfiguration.native.freeze(),
            constraints.native.freeze(),
            PeerConnectionObserver(pcEvents).freeze()
        ).freeze()

        return PeerConnection(nativePc, pcEvents)
    }

    actual fun createLocalMediaStream(id: String): MediaStream {
        return MediaStream(native.mediaStreamWithStreamId(id).freeze()).freeze()
    }

    actual fun createVideoSource(
        isScreencast: Boolean,
        alignTimestamps: Boolean
    ): VideoSource {
        return VideoSource(native.videoSource().freeze())
    }

    actual fun createVideoTrack(id: String, videoSource: VideoSource): VideoTrack {
        return VideoTrack(native.videoTrackWithSource(videoSource.native, id).freeze())
    }

    actual fun createAudioSource(constraints: MediaConstraints): AudioSource {
        return AudioSource(native.audioSourceWithConstraints(constraints.native).freeze())
    }

    actual fun createAudioTrack(id: String, audioSource: AudioSource): AudioTrack {
        return AudioTrack(native.audioTrackWithSource(audioSource.native, id).freeze())
    }

    actual fun startAecDump(filePath: String, fileSizeLimitBytes: Int) {
        native.startAecDumpWithFilePath(filePath, fileSizeLimitBytes.toLong())
    }

    actual fun stopAecDump() = native.stopAecDump()
}
