package com.shepeliev.webrtckmp

import WebRTC.RTCCleanupSSL
import WebRTC.RTCDefaultVideoDecoderFactory
import WebRTC.RTCDefaultVideoEncoderFactory
import WebRTC.RTCInitializeSSL
import WebRTC.RTCLogEx
import WebRTC.RTCLoggingSeverity
import WebRTC.RTCPeerConnectionFactory
import WebRTC.RTCPeerConnectionFactoryOptions
import WebRTC.RTCShutdownInternalTracer
import WebRTC.RTCVideoDecoderFactoryProtocol
import WebRTC.RTCVideoEncoderFactoryProtocol

@ThreadLocal
object WebRtc {
    var videoEncoderFactory: RTCVideoEncoderFactoryProtocol? = null
        set(value) {
            field = value
            if (_peerConnectionFactory != null) {
                RTCLogEx(
                    RTCLoggingSeverity.RTCLoggingSeverityError,
                    "Peer connection factory is already initialized. " +
                        "Setting video encoder factory after initialization has no effect."
                )
            }
        }

    var videoDecoderFactory: RTCVideoDecoderFactoryProtocol? = null
        set(value) {
            field = value
            if (_peerConnectionFactory != null) {
                RTCLogEx(
                    RTCLoggingSeverity.RTCLoggingSeverityError,
                    "Peer connection factory is already initialized. " +
                        "Setting video decoder factory after initialization has no effect."
                )
            }
        }

    var peerConnectionFactoryOptions: RTCPeerConnectionFactoryOptions? = null
        set(value) {
            field = value
            if (_peerConnectionFactory != null) {
                RTCLogEx(
                    RTCLoggingSeverity.RTCLoggingSeverityError,
                    "Peer connection factory is already initialized. " +
                        "Setting peer connection factory options after initialization has no effect."
                )
            }
        }

    private var _peerConnectionFactory: RTCPeerConnectionFactory? = null
    internal val peerConnectionFactory: RTCPeerConnectionFactory
        get() {
            if (_peerConnectionFactory == null) initialize()
            return checkNotNull(_peerConnectionFactory)
        }

    private fun initialize() {
        RTCInitializeSSL()
        _peerConnectionFactory = RTCPeerConnectionFactory(
            videoEncoderFactory ?: RTCDefaultVideoEncoderFactory(),
            videoDecoderFactory ?: RTCDefaultVideoDecoderFactory()
        ).apply {
            peerConnectionFactoryOptions?.let { setOptions(it) }
        }
    }

    fun disposePeerConnectionFactory() {
        RTCShutdownInternalTracer()
        RTCCleanupSSL()
    }
}
