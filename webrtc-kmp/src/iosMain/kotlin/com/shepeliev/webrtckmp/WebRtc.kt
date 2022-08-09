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

@Deprecated("It will be removed in one of the future releases.")
actual object WebRtc {

    @Deprecated(
        message = "Use MediaDevices companion object.",
        replaceWith = ReplaceWith("MediaDevices")
    )
    actual val mediaDevices: MediaDevices = MediaDevices
}

fun initializeWebRtc(build: WebRtcBuilder.() -> Unit = {}) {
    build(webRtcBuilder)
}

val factory: RTCPeerConnectionFactory by lazy {
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
        RTCDefaultVideoEncoderFactory(),
        RTCDefaultVideoDecoderFactory()
    )
    options?.also { factory.setOptions(options) }
    return factory
}
