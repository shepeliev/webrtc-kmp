package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCCleanupSSL
import cocoapods.GoogleWebRTC.RTCDefaultVideoDecoderFactory
import cocoapods.GoogleWebRTC.RTCDefaultVideoEncoderFactory
import cocoapods.GoogleWebRTC.RTCInitFieldTrialDictionary
import cocoapods.GoogleWebRTC.RTCInitializeSSL
import cocoapods.GoogleWebRTC.RTCLoggingSeverity
import cocoapods.GoogleWebRTC.RTCPeerConnectionFactory
import cocoapods.GoogleWebRTC.RTCPeerConnectionFactoryOptions
import cocoapods.GoogleWebRTC.RTCSetMinDebugLogLevel
import cocoapods.GoogleWebRTC.RTCSetupInternalTracer
import cocoapods.GoogleWebRTC.RTCShutdownInternalTracer
import platform.Foundation.NSBundle

actual class PeerConnectionFactory private constructor(val native: RTCPeerConnectionFactory) {
    actual companion object {

        @Suppress("UNCHECKED_CAST")
        actual fun build(options: Options?): PeerConnectionFactory {

            val trials = NSBundle.mainBundle
                .objectForInfoDictionaryKey("WebRtcKMM_FieldTrials") as? Map<Any?, *>
                ?: emptyMap<Any?, Any?>()

            RTCInitFieldTrialDictionary(trials)
            RTCInitializeSSL()
            RTCSetupInternalTracer()

            if (!Platform.isDebugBinary) {
                RTCSetMinDebugLogLevel(RTCLoggingSeverity.RTCLoggingSeverityWarning)
            }

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

        actual fun dispose() {
            RTCShutdownInternalTracer()
            RTCCleanupSSL()
        }
    }

    actual fun createPeerConnection(rtcConfiguration: RtcConfiguration): PeerConnection {
        return PeerConnection().apply {
            val constraints = mediaConstraints {
                optional { "RtpDataChannels" to "${rtcConfiguration.enableRtpDataChannel}" }
                rtcConfiguration.enableDtlsSrtp?.let { optional { "DtlsSrtpKeyAgreement" to "$it" } }
            }

            native = peerConnectionFactory.native.peerConnectionWithConfiguration(
                rtcConfiguration.native,
                constraints.native,
                pcObserver
            )
        }
    }

    actual fun createLocalMediaStream(id: String): MediaStream {
        return MediaStream(native.mediaStreamWithStreamId(id))
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
}
