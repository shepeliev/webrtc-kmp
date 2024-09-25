package com.shepeliev.webrtckmp

import WebRTC.RTCInitializeSSL
import WebRTC.RTCLoggingSeverity
import WebRTC.RTCPeerConnectionFactory
import WebRTC.RTCSetMinDebugLogLevel
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
object WebRtc {
    internal var videoProcessorFactory: VideoProcessorFactory? = null
        private set

    private var _peerConnectionFactory: RTCPeerConnectionFactory? = null
    internal val peerConnectionFactory: RTCPeerConnectionFactory by lazy {
        _peerConnectionFactory ?: run {
            initialize()
            RTCPeerConnectionFactory().also { _peerConnectionFactory = it }
        }
    }

    /**
     * The name of the bundled video file to use as a fallback for the camera in the iOS simulator.
     */
    var simulatorCameraFallbackFileName: String = "simulator-camera.mp4"

    /**
     * Configures the WebRTC KMP library with the specified parameters.
     *
     * @param loggingSeverity The severity of the logging output.
     */
    fun configure(loggingSeverity: RTCLoggingSeverity) {
        configureInternal(loggingSeverity = loggingSeverity)
    }

    /**
     * Configures the WebRTC KMP library with the specified parameters.
     *
     * @param videoProcessorFactory The factory to create video processors.
     */
    fun configure(videoProcessorFactory: VideoProcessorFactory) {
        configureInternal(videoProcessorFactory = videoProcessorFactory)
    }

    /**
     * Configures the WebRTC KMP library with the specified parameters.
     *
     * @param rtcPeerConnectionFactory The peer connection factory to use.
     */
    fun configure(rtcPeerConnectionFactory: RTCPeerConnectionFactory) {
        configureInternal(rtcPeerConnectionFactory = rtcPeerConnectionFactory)
    }

    /**
     * Configures the WebRTC KMP library with the specified parameters.
     *
     * @param loggingSeverity The severity of the logging output.
     * @param videoProcessorFactory The factory to create video processors.
     */
    fun configure(
        loggingSeverity: RTCLoggingSeverity,
        videoProcessorFactory: VideoProcessorFactory,
    ) {
        configureInternal(
            loggingSeverity = loggingSeverity,
            videoProcessorFactory = videoProcessorFactory
        )
    }

    /**
     * Configures the WebRTC KMP library with the specified parameters.
     *
     * @param loggingSeverity The severity of the logging output.
     * @param rtcPeerConnectionFactory The peer connection factory to use.
     */
    fun configure(
        loggingSeverity: RTCLoggingSeverity,
        rtcPeerConnectionFactory: RTCPeerConnectionFactory,
    ) {
        configureInternal(
            loggingSeverity = loggingSeverity,
            rtcPeerConnectionFactory = rtcPeerConnectionFactory
        )
    }

    /**
     * Configures the WebRTC KMP library with the specified parameters.
     *
     * @param videoProcessorFactory The factory to create video processors.
     * @param rtcPeerConnectionFactory The peer connection factory to use.
     */
    fun configure(
        videoProcessorFactory: VideoProcessorFactory,
        rtcPeerConnectionFactory: RTCPeerConnectionFactory,
    ) {
        configureInternal(
            videoProcessorFactory = videoProcessorFactory,
            rtcPeerConnectionFactory = rtcPeerConnectionFactory
        )
    }

    /**
     * Configures the WebRTC KMP library with the specified parameters.
     *
     * @param loggingSeverity The severity of the logging output.
     * @param videoProcessorFactory The factory to create video processors.
     * @param rtcPeerConnectionFactory The peer connection factory to use.
     */
    fun configure(
        loggingSeverity: RTCLoggingSeverity,
        videoProcessorFactory: VideoProcessorFactory,
        rtcPeerConnectionFactory: RTCPeerConnectionFactory,
    ) {
        configureInternal(
            loggingSeverity = loggingSeverity,
            videoProcessorFactory = videoProcessorFactory,
            rtcPeerConnectionFactory = rtcPeerConnectionFactory,
        )
    }

    private fun configureInternal(
        loggingSeverity: RTCLoggingSeverity? = null,
        videoProcessorFactory: VideoProcessorFactory? = null,
        rtcPeerConnectionFactory: RTCPeerConnectionFactory? = null,
    ) {
        check(_peerConnectionFactory == null) {
            "WebRtc.configure() must be called once only and before any access to MediaDevices."
        }

        this.videoProcessorFactory = videoProcessorFactory
        _peerConnectionFactory = rtcPeerConnectionFactory
        initialize(loggingSeverity)
    }

    private fun initialize(loggingSeverity: RTCLoggingSeverity? = null) {
        RTCInitializeSSL()
        loggingSeverity?.let { RTCSetMinDebugLogLevel(it) }
    }
}
