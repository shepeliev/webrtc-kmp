package com.shepeliev.webrtckmp

import WebRTC.RTCDefaultVideoDecoderFactory
import WebRTC.RTCDefaultVideoEncoderFactory
import WebRTC.RTCInitFieldTrialDictionary
import WebRTC.RTCInitializeSSL
import WebRTC.RTCLoggingSeverity
import WebRTC.RTCPeerConnectionFactory
import WebRTC.RTCPeerConnectionFactoryOptions
import WebRTC.RTCSetMinDebugLogLevel
import WebRTC.RTCSetupInternalTracer
import kotlin.native.concurrent.freeze

actual object WebRtc {
    actual val mediaDevices: MediaDevices = MediaDevicesImpl
}

fun initializeWebRtc(build: WebRtcBuilder.() -> Unit = {}) {
    build(webRtcBuilder)
}

internal val factory: RTCPeerConnectionFactory by lazy {
    initializePeerConnectionFactory()
    buildPeerConnectionFactory(webRtcBuilder.peerConnectionFactoryOptions)
}

class WebRtcBuilder(
    var peerConnectionFactoryOptions: RTCPeerConnectionFactoryOptions? = null,
    var fieldTrials: Map<String, String> = emptyMap(),
    var enableInternalTracer: Boolean = false,
    var loggingSeverity: RTCLoggingSeverity? = null
)

private val webRtcBuilder = WebRtcBuilder()

@Suppress("UNCHECKED_CAST")
private fun initializePeerConnectionFactory() {
    with(webRtcBuilder) {
        RTCInitFieldTrialDictionary(fieldTrials as Map<Any?, *>)
        RTCInitializeSSL()
        loggingSeverity?.also { RTCSetMinDebugLogLevel(it) }
        if (enableInternalTracer) {
            RTCSetupInternalTracer()
        }
    }
}

private fun buildPeerConnectionFactory(options: RTCPeerConnectionFactoryOptions?): RTCPeerConnectionFactory {
    val factory = RTCPeerConnectionFactory(
        RTCDefaultVideoEncoderFactory().freeze(),
        RTCDefaultVideoDecoderFactory().freeze()
    )
    options?.also { factory.setOptions(options.freeze()) }
    return factory
}
