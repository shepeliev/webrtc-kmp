package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCDefaultVideoDecoderFactory
import cocoapods.GoogleWebRTC.RTCDefaultVideoEncoderFactory
import cocoapods.GoogleWebRTC.RTCPeerConnectionFactory
import cocoapods.GoogleWebRTC.RTCPeerConnectionFactoryOptions

actual class PeerConnectionFactory private constructor(val native: RTCPeerConnectionFactory) {
    actual companion object {
        actual fun build(options: Options?): PeerConnectionFactory {
            val native = RTCPeerConnectionFactory(
                RTCDefaultVideoEncoderFactory(),
                RTCDefaultVideoDecoderFactory()
            )
            options?.let {
                native.setOptions(
                    RTCPeerConnectionFactoryOptions().apply {
                        disableNetworkMonitor = it.disableNetworkMonitor
                        disableEncryption = it.disableEncryption
                        ignoreCellularNetworkAdapter = it.ignoreCellularNetworkAdapter
                        ignoreEthernetNetworkAdapter = it.ignoreEthernetNetworkAdapter
                        ignoreLoopbackNetworkAdapter = it.ignoreLoopbackNetworkAdapter
                        ignoreVPNNetworkAdapter = it.ignoreVpnNetworkAdapter
                        ignoreWiFiNetworkAdapter = it.ignoreWiFiNetworkAdapter
                    }
                )
            }

            return PeerConnectionFactory(native)
        }
    }

//    actual fun createPeerConnection(
//        rtcConfig: RtcConfiguration,
//        observer: PeerConnectionObserver
//    ): RtcPeerConnection? {
//
//        val constraints = mediaConstraints {
//            optional { "RtpDataChannels" to "${rtcConfig.enableRtpDataChannel}" }
//            rtcConfig.enableDtlsSrtp?.let { optional { "DtlsSrtpKeyAgreement" to "$it" } }
//        }
//
//        return native.peerConnectionWithConfiguration(
//            rtcConfig.native,
//            constraints.native,
//            CommonPeerConnectionObserverAdapter(observer)
//        ).let { RtcPeerConnection(it) }
//    }

    actual fun createLocalMediaStream(label: String): MediaStream {
        return MediaStream(native.mediaStreamWithStreamId(label))
    }

    actual fun createVideoSource(
        isScreencast: Boolean,
        alignTimestamps: Boolean
    ): VideoSource {
        return VideoSource(native.videoSource())
    }

    actual fun createVideoTrack(id: String, videoSource: VideoSource): VideoTrack {
        return VideoTrack(native.videoTrackWithSource(videoSource.native, id))
    }

    actual fun createAudioSource(constraints: MediaConstraints): AudioSource {
        return AudioSource(native.audioSourceWithConstraints(constraints.native))
    }

    actual fun createAudioTrack(id: String, audioSource: AudioSource): AudioTrack {
        return AudioTrack(native.audioTrackWithSource(audioSource.native, id))
    }

    actual fun startAecDump(filePath: String, fileSizeLimitBytes: Int) {
        native.startAecDumpWithFilePath(filePath, fileSizeLimitBytes.toLong())
    }

    actual fun stopAecDump() = native.stopAecDump()

    actual fun dispose() {
        // not applicable
    }
}
