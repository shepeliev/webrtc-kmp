package com.shepeliev.webrtckmp

import WebRTC.RTCCleanupSSL
import WebRTC.RTCDefaultVideoDecoderFactory
import WebRTC.RTCDefaultVideoEncoderFactory
import WebRTC.RTCInitFieldTrialDictionary
import WebRTC.RTCInitializeSSL
import WebRTC.RTCLoggingSeverity
import WebRTC.RTCPeerConnectionFactory
import WebRTC.RTCPeerConnectionFactoryOptions
import WebRTC.RTCSetMinDebugLogLevel
import WebRTC.RTCSetupInternalTracer
import WebRTC.RTCShutdownInternalTracer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlin.native.concurrent.AtomicReference
import kotlin.native.concurrent.freeze

actual object WebRtcKmp {
    actual val mainScope: CoroutineScope
        get() {
            val scope = mainScopeRef.value
            check(scope != null) { NOT_INITIALIZED_ERROR_MESSAGE }
            return scope
        }

    internal actual val peerConnectionFactory: PeerConnectionFactory
        get() {
            check(peerConnectionFactoryInternal != null) { NOT_INITIALIZED_ERROR_MESSAGE }
            return peerConnectionFactoryInternal!!
        }
}

private val mainScopeRef = AtomicReference<CoroutineScope?>(null)
private var peerConnectionFactoryInternal: PeerConnectionFactory? = null

fun WebRtcKmp.initialize(
    peerConnectionFactoryOptions: RTCPeerConnectionFactoryOptions? = null,
    fieldTrials: Map<String, String> = emptyMap(),
    enableInternalTracer: Boolean = false,
    loggingSeverity: RTCLoggingSeverity? = null,
) {
    mainScopeRef.value = MainScope().freeze()
    initializeIosLib(fieldTrials, enableInternalTracer, loggingSeverity)
    buildPeerConnectionFactory(peerConnectionFactoryOptions)
}

private fun initializeIosLib(
    fieldTrials: Map<String, String>,
    enableInternalTracer: Boolean,
    loggingSeverity: RTCLoggingSeverity?,
) {
    RTCInitFieldTrialDictionary(fieldTrials as Map<Any?, *>)
    RTCInitializeSSL()
    loggingSeverity?.also { RTCSetMinDebugLogLevel(it) }
    if (enableInternalTracer) {
        RTCSetupInternalTracer()
    }
}

private fun buildPeerConnectionFactory(options: RTCPeerConnectionFactoryOptions?) {
    val iosPeerConnectionFactory = RTCPeerConnectionFactory(
        RTCDefaultVideoEncoderFactory().freeze(),
        RTCDefaultVideoDecoderFactory().freeze()
    )
    options?.also { iosPeerConnectionFactory.setOptions(options.freeze()) }
    peerConnectionFactoryInternal = PeerConnectionFactory(iosPeerConnectionFactory).freeze()
}

fun WebRtcKmp.dispose() {
    peerConnectionFactoryInternal = null
    RTCShutdownInternalTracer()
    RTCCleanupSSL()
    mainScopeRef.value?.cancel()
    mainScopeRef.value = null
}
