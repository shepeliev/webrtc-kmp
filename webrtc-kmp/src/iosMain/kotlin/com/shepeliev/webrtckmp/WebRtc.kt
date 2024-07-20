@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCDefaultVideoDecoderFactory
import WebRTC.RTCDefaultVideoEncoderFactory
import WebRTC.RTCInitializeSSL
import WebRTC.RTCPeerConnectionFactory
import WebRTC.RTCPeerConnectionFactoryOptions
import WebRTC.RTCVideoDecoderFactoryProtocol
import WebRTC.RTCVideoEncoderFactoryProtocol
import kotlinx.cinterop.ExperimentalForeignApi

object WebRtc {
    var videoEncoderFactory: RTCVideoEncoderFactoryProtocol? = null
    var videoDecoderFactory: RTCVideoDecoderFactoryProtocol? = null
    var peerConnectionFactoryOptions: RTCPeerConnectionFactoryOptions? = null
    var customPeerConnectionFactory: RTCPeerConnectionFactory? = null
    var videoProcessorFactory: VideoProcessorFactory? = null

    internal val peerConnectionFactory: RTCPeerConnectionFactory by lazy {
        customPeerConnectionFactory ?: run {
            RTCInitializeSSL()
            RTCPeerConnectionFactory(
                videoEncoderFactory ?: RTCDefaultVideoEncoderFactory(),
                videoDecoderFactory ?: RTCDefaultVideoDecoderFactory()
            ).apply {
                peerConnectionFactoryOptions?.let { setOptions(it) }
            }
        }
    }
}
