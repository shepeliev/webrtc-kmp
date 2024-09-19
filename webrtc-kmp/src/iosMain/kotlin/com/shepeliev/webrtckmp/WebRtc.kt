package com.shepeliev.webrtckmp

import WebRTC.RTCDefaultVideoDecoderFactory
import WebRTC.RTCDefaultVideoEncoderFactory
import WebRTC.RTCInitializeSSL
import WebRTC.RTCLoggingSeverity
import WebRTC.RTCPeerConnectionFactory
import WebRTC.RTCPeerConnectionFactoryOptions
import WebRTC.RTCSetMinDebugLogLevel
import WebRTC.RTCVideoDecoderFactoryProtocol
import WebRTC.RTCVideoEncoderFactoryProtocol
import com.shepeliev.webrtckmp.video.VideoProcessorFactory
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
object WebRtc {
    private var videoEncoderFactory: RTCVideoEncoderFactoryProtocol? = null
    private var videoDecoderFactory: RTCVideoDecoderFactoryProtocol? = null
    private var peerConnectionFactoryOptions: RTCPeerConnectionFactoryOptions? = null
    private var loggingSeverity: RTCLoggingSeverity? = null

    internal var videoProcessorFactory: VideoProcessorFactory? = null
        private set

    private var _peerConnectionFactory: RTCPeerConnectionFactory? = null
    internal val peerConnectionFactory: RTCPeerConnectionFactory
        get() = _peerConnectionFactory ?: createPeerConnectionFactory().also { _peerConnectionFactory = it }

    /**
     * The name of the bundled video file to use as a fallback for the camera in the iOS simulator.
     */
    var simulatorCameraFallbackFileName: String = "simulator-camera.mp4"

    @Suppress("unused")
    fun configurePeerConnectionFactory(loggingSeverity: RTCLoggingSeverity) {
        configurePeerConnectionFactoryInternal(loggingSeverity, null, null, null, null)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(videoEncoderFactory: RTCVideoEncoderFactoryProtocol) {
        configurePeerConnectionFactoryInternal(null, videoEncoderFactory, null, null, null)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(videoDecoderFactory: RTCVideoDecoderFactoryProtocol) {
        configurePeerConnectionFactoryInternal(null, null, videoDecoderFactory, null, null)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(options: RTCPeerConnectionFactoryOptions) {
        configurePeerConnectionFactoryInternal(null, null, null, options, null)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        videoProcessorFactory: VideoProcessorFactory
    ) {
        configurePeerConnectionFactoryInternal(null, null, null, null, videoProcessorFactory)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        loggingSeverity: RTCLoggingSeverity,
        videoEncoderFactory: RTCVideoEncoderFactoryProtocol
    ) {
        configurePeerConnectionFactoryInternal(loggingSeverity, videoEncoderFactory, null, null, null)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        loggingSeverity: RTCLoggingSeverity,
        videoDecoderFactory: RTCVideoDecoderFactoryProtocol
    ) {
        configurePeerConnectionFactoryInternal(loggingSeverity, null, videoDecoderFactory, null, null)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        loggingSeverity: RTCLoggingSeverity,
        options: RTCPeerConnectionFactoryOptions
    ) {
        configurePeerConnectionFactoryInternal(loggingSeverity, null, null, options, null)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        loggingSeverity: RTCLoggingSeverity,
        videoProcessorFactory: VideoProcessorFactory
    ) {
        configurePeerConnectionFactoryInternal(loggingSeverity, null, null, null, videoProcessorFactory)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        videoEncoderFactory: RTCVideoEncoderFactoryProtocol,
        videoDecoderFactory: RTCVideoDecoderFactoryProtocol
    ) {
        configurePeerConnectionFactoryInternal(null, videoEncoderFactory, videoDecoderFactory, null, null)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        videoEncoderFactory: RTCVideoEncoderFactoryProtocol,
        options: RTCPeerConnectionFactoryOptions
    ) {
        configurePeerConnectionFactoryInternal(null, videoEncoderFactory, null, options, null)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        videoEncoderFactory: RTCVideoEncoderFactoryProtocol,
        videoProcessorFactory: VideoProcessorFactory
    ) {
        configurePeerConnectionFactoryInternal(null, videoEncoderFactory, null, null, videoProcessorFactory)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        videoDecoderFactory: RTCVideoDecoderFactoryProtocol,
        options: RTCPeerConnectionFactoryOptions
    ) {
        configurePeerConnectionFactoryInternal(null, null, videoDecoderFactory, options, null)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        videoDecoderFactory: RTCVideoDecoderFactoryProtocol,
        videoProcessorFactory: VideoProcessorFactory
    ) {
        configurePeerConnectionFactoryInternal(null, null, videoDecoderFactory, null, videoProcessorFactory)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        options: RTCPeerConnectionFactoryOptions,
        videoProcessorFactory: VideoProcessorFactory
    ) {
        configurePeerConnectionFactoryInternal(null, null, null, options, videoProcessorFactory)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        loggingSeverity: RTCLoggingSeverity,
        videoEncoderFactory: RTCVideoEncoderFactoryProtocol,
        videoDecoderFactory: RTCVideoDecoderFactoryProtocol
    ) {
        configurePeerConnectionFactoryInternal(loggingSeverity, videoEncoderFactory, videoDecoderFactory, null, null)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        loggingSeverity: RTCLoggingSeverity,
        videoEncoderFactory: RTCVideoEncoderFactoryProtocol,
        options: RTCPeerConnectionFactoryOptions
    ) {
        configurePeerConnectionFactoryInternal(loggingSeverity, videoEncoderFactory, null, options, null)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        loggingSeverity: RTCLoggingSeverity,
        videoEncoderFactory: RTCVideoEncoderFactoryProtocol,
        videoProcessorFactory: VideoProcessorFactory
    ) {
        configurePeerConnectionFactoryInternal(loggingSeverity, videoEncoderFactory, null, null, videoProcessorFactory)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        loggingSeverity: RTCLoggingSeverity,
        videoDecoderFactory: RTCVideoDecoderFactoryProtocol,
        options: RTCPeerConnectionFactoryOptions
    ) {
        configurePeerConnectionFactoryInternal(loggingSeverity, null, videoDecoderFactory, options, null)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        loggingSeverity: RTCLoggingSeverity,
        videoDecoderFactory: RTCVideoDecoderFactoryProtocol,
        videoProcessorFactory: VideoProcessorFactory
    ) {
        configurePeerConnectionFactoryInternal(loggingSeverity, null, videoDecoderFactory, null, videoProcessorFactory)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        loggingSeverity: RTCLoggingSeverity,
        options: RTCPeerConnectionFactoryOptions,
        videoProcessorFactory: VideoProcessorFactory
    ) {
        configurePeerConnectionFactoryInternal(loggingSeverity, null, null, options, videoProcessorFactory)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        videoEncoderFactory: RTCVideoEncoderFactoryProtocol,
        videoDecoderFactory: RTCVideoDecoderFactoryProtocol,
        options: RTCPeerConnectionFactoryOptions
    ) {
        configurePeerConnectionFactoryInternal(null, videoEncoderFactory, videoDecoderFactory, options, null)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        videoEncoderFactory: RTCVideoEncoderFactoryProtocol,
        videoDecoderFactory: RTCVideoDecoderFactoryProtocol,
        videoProcessorFactory: VideoProcessorFactory
    ) {
        configurePeerConnectionFactoryInternal(
            null,
            videoEncoderFactory,
            videoDecoderFactory,
            null,
            videoProcessorFactory
        )
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        videoEncoderFactory: RTCVideoEncoderFactoryProtocol,
        options: RTCPeerConnectionFactoryOptions,
        videoProcessorFactory: VideoProcessorFactory
    ) {
        configurePeerConnectionFactoryInternal(null, videoEncoderFactory, null, options, videoProcessorFactory)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        videoDecoderFactory: RTCVideoDecoderFactoryProtocol,
        options: RTCPeerConnectionFactoryOptions,
        videoProcessorFactory: VideoProcessorFactory
    ) {
        configurePeerConnectionFactoryInternal(null, null, videoDecoderFactory, options, videoProcessorFactory)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        loggingSeverity: RTCLoggingSeverity,
        videoEncoderFactory: RTCVideoEncoderFactoryProtocol,
        videoDecoderFactory: RTCVideoDecoderFactoryProtocol,
        options: RTCPeerConnectionFactoryOptions
    ) {
        configurePeerConnectionFactoryInternal(loggingSeverity, videoEncoderFactory, videoDecoderFactory, options, null)
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        loggingSeverity: RTCLoggingSeverity,
        videoEncoderFactory: RTCVideoEncoderFactoryProtocol,
        videoDecoderFactory: RTCVideoDecoderFactoryProtocol,
        videoProcessorFactory: VideoProcessorFactory
    ) {
        configurePeerConnectionFactoryInternal(
            loggingSeverity,
            videoEncoderFactory,
            videoDecoderFactory,
            null,
            videoProcessorFactory
        )
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        loggingSeverity: RTCLoggingSeverity,
        videoEncoderFactory: RTCVideoEncoderFactoryProtocol,
        options: RTCPeerConnectionFactoryOptions,
        videoProcessorFactory: VideoProcessorFactory
    ) {
        configurePeerConnectionFactoryInternal(
            loggingSeverity,
            videoEncoderFactory,
            null,
            options,
            videoProcessorFactory
        )
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        loggingSeverity: RTCLoggingSeverity,
        videoDecoderFactory: RTCVideoDecoderFactoryProtocol,
        options: RTCPeerConnectionFactoryOptions,
        videoProcessorFactory: VideoProcessorFactory
    ) {
        configurePeerConnectionFactoryInternal(
            loggingSeverity,
            null,
            videoDecoderFactory,
            options,
            videoProcessorFactory
        )
    }

    @Suppress("unused")
    fun configurePeerConnectionFactory(
        videoEncoderFactory: RTCVideoEncoderFactoryProtocol,
        videoDecoderFactory: RTCVideoDecoderFactoryProtocol,
        options: RTCPeerConnectionFactoryOptions,
        videoProcessorFactory: VideoProcessorFactory
    ) {
        configurePeerConnectionFactoryInternal(
            null,
            videoEncoderFactory,
            videoDecoderFactory,
            options,
            videoProcessorFactory
        )
    }

    @Suppress("unused")
    private fun configurePeerConnectionFactory(
        loggingSeverity: RTCLoggingSeverity,
        videoEncoderFactory: RTCVideoEncoderFactoryProtocol,
        videoDecoderFactory: RTCVideoDecoderFactoryProtocol,
        options: RTCPeerConnectionFactoryOptions,
        videoProcessorFactory: VideoProcessorFactory,
    ) {
        configurePeerConnectionFactoryInternal(
            loggingSeverity,
            videoEncoderFactory,
            videoDecoderFactory,
            options,
            videoProcessorFactory
        )
    }

    private fun configurePeerConnectionFactoryInternal(
        loggingSeverity: RTCLoggingSeverity?,
        videoEncoderFactory: RTCVideoEncoderFactoryProtocol?,
        videoDecoderFactory: RTCVideoDecoderFactoryProtocol?,
        options: RTCPeerConnectionFactoryOptions?,
        videoProcessorFactory: VideoProcessorFactory?,
    ) {
        check(_peerConnectionFactory == null) {
            "WebRtc.configurePeerConnectionFactory() must be called once only and before any access to MediaDevices."
        }

        this.loggingSeverity = loggingSeverity
        this.videoEncoderFactory = videoEncoderFactory
        this.videoDecoderFactory = videoDecoderFactory
        this.peerConnectionFactoryOptions = options
        this.videoProcessorFactory = videoProcessorFactory
        _peerConnectionFactory = createPeerConnectionFactory()
    }

    private fun createPeerConnectionFactory(): RTCPeerConnectionFactory {
        RTCInitializeSSL()
        loggingSeverity?.let { RTCSetMinDebugLogLevel(it) }
        val encoderFactory = videoEncoderFactory ?: RTCDefaultVideoEncoderFactory()
        val decoderFactory = videoDecoderFactory ?: RTCDefaultVideoDecoderFactory()
        val factory = RTCPeerConnectionFactory(encoderFactory, decoderFactory)
        peerConnectionFactoryOptions?.let { factory.setOptions(it) }
        return factory
    }
}
