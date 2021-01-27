package com.shepeliev.webrtckmm

import kotlin.jvm.JvmOverloads

data class Options @JvmOverloads constructor(
    val disableEncryption: Boolean = false,
    val disableNetworkMonitor: Boolean = false,
    val ignoreEthernetNetworkAdapter: Boolean = false,
    val ignoreWiFiNetworkAdapter: Boolean = false,
    val ignoreVpnNetworkAdapter: Boolean = false,
    val ignoreLoopbackNetworkAdapter: Boolean = false,

    // Android only
    val ignoreCellularNetworkAdapter: Boolean = false,
) {
    companion object {
        const val ADAPTER_TYPE_UNKNOWN = 0
        const val ADAPTER_TYPE_ETHERNET = 1 shl 0
        const val ADAPTER_TYPE_WIFI = 1 shl 1
        const val ADAPTER_TYPE_CELLULAR = 1 shl 2
        const val ADAPTER_TYPE_VPN = 1 shl 3
        const val ADAPTER_TYPE_LOOPBACK = 1 shl 4
        const val ADAPTER_TYPE_ANY = 1 shl 5
    }
}

expect class PeerConnectionFactory {

    companion object {
        fun initialize(
            context: Any? = null,
            fieldTrials: String = "",
            enableInternalTracer: Boolean = false,
        )

        fun build(
            options: Options? = null,
            eglContext: Any? = null,
            audioDeviceModule: Any? = null,
        ): PeerConnectionFactory
    }

    fun createPeerConnection(
        rtcConfig: RtcConfiguration,
        observer: PeerConnectionObserver
    ): PeerConnection?

    fun createLocalMediaStream(label: String): MediaStream
    fun createVideoSource(isScreencast: Boolean, alignTimestamps: Boolean = true): VideoSource
    fun createVideoTrack(id: String, videoSource: VideoSource): VideoTrack
    fun createAudioSource(constraints: MediaConstraints): AudioSource
    fun createAudioTrack(id: String, audioSource: AudioSource): AudioTrack

    fun startAecDump(filePath: String, fileSizeLimitBytes: Int)
    fun stopAecDump()
    /* TODO
    fun printInternalStackTraces(printNativeStackTraces: Boolean)
     */

    fun dispose()
}
