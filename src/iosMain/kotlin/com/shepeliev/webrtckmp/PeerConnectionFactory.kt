package com.shepeliev.webrtckmp

import WebRTC.RTCDefaultVideoDecoderFactory
import WebRTC.RTCDefaultVideoEncoderFactory
import WebRTC.RTCPeerConnectionFactory
import WebRTC.RTCPeerConnectionFactoryOptions
import kotlin.native.concurrent.freeze

internal actual class PeerConnectionFactory private constructor(
    val native: RTCPeerConnectionFactory,
) {

    actual companion object {
        @Suppress("UNCHECKED_CAST")
        actual fun build(options: Options?): PeerConnectionFactory {
            val native = RTCPeerConnectionFactory(
                RTCDefaultVideoEncoderFactory().freeze(),
                RTCDefaultVideoDecoderFactory().freeze()
            )
            if (options != null) {
                val nativeOptions = RTCPeerConnectionFactoryOptions().apply {
                    disableNetworkMonitor = options.disableNetworkMonitor
                    disableEncryption = options.disableEncryption
                    ignoreCellularNetworkAdapter = options.ignoreCellularNetworkAdapter
                    ignoreEthernetNetworkAdapter = options.ignoreEthernetNetworkAdapter
                    ignoreLoopbackNetworkAdapter = options.ignoreLoopbackNetworkAdapter
                    ignoreVPNNetworkAdapter = options.ignoreVpnNetworkAdapter
                    ignoreWiFiNetworkAdapter = options.ignoreWiFiNetworkAdapter
                }
                native.setOptions(nativeOptions.freeze())
            }

            return PeerConnectionFactory(native)
        }
    }

    actual fun createPeerConnection(
        rtcConfiguration: RtcConfiguration,
        constraints: MediaConstraints,
    ): PeerConnection {
        val pcEvents = PeerConnectionEvents().freeze()
        val nativePc = peerConnectionFactory.native.peerConnectionWithConfiguration(
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

    actual fun dispose() {
        // not applicable for iOS
    }
}
