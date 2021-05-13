package com.shepeliev.webrtckmp

import WebRTC.RTCCleanupSSL
import WebRTC.RTCInitFieldTrialDictionary
import WebRTC.RTCInitializeSSL
import WebRTC.RTCLoggingSeverity
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
}

private val mainScopeRef = AtomicReference<CoroutineScope?>(null)

fun WebRtcKmp.initialize(
    fieldTrials: Map<String, String> = emptyMap(),
    enableInternalTracer: Boolean = false,
    loggingSeverity: RTCLoggingSeverity? = null,
) {
    mainScopeRef.value = MainScope().freeze()

    RTCInitFieldTrialDictionary(fieldTrials as Map<Any?, *>)
    RTCInitializeSSL()

    loggingSeverity?.also { RTCSetMinDebugLogLevel(it) }

    if (enableInternalTracer) {
        RTCSetupInternalTracer()
    }
}

fun WebRtcKmp.dispose() {
    RTCShutdownInternalTracer()
    RTCCleanupSSL()
    mainScopeRef.value?.cancel()
    mainScopeRef.value = null
}

