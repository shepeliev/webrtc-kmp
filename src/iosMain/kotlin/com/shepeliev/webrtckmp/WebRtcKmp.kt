package com.shepeliev.webrtckmp

import WebRTC.RTCCleanupSSL
import WebRTC.RTCInitFieldTrialDictionary
import WebRTC.RTCInitializeSSL
import WebRTC.RTCLoggingSeverity
import WebRTC.RTCSetMinDebugLogLevel
import WebRTC.RTCSetupInternalTracer
import WebRTC.RTCShutdownInternalTracer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext
import kotlin.native.concurrent.AtomicReference
import kotlin.native.concurrent.freeze

actual object WebRtcKmp {
    @Suppress("UNCHECKED_CAST")
    actual fun initialize(
        fieldTrials: Map<String, String>,
        enableInternalTracer: Boolean,
        loggingSeverity: WebRtcLoggingSeverity,
    ) {
        coroutineScopeRef.value = IosMainScope().freeze()

        if (loggingSeverity != WebRtcLoggingSeverity.None) {
            val severity = when (loggingSeverity) {
                WebRtcLoggingSeverity.Verbose -> RTCLoggingSeverity.RTCLoggingSeverityVerbose
                WebRtcLoggingSeverity.Info -> RTCLoggingSeverity.RTCLoggingSeverityInfo
                WebRtcLoggingSeverity.Warning -> RTCLoggingSeverity.RTCLoggingSeverityWarning
                WebRtcLoggingSeverity.Error -> RTCLoggingSeverity.RTCLoggingSeverityError
                WebRtcLoggingSeverity.None -> RTCLoggingSeverity.RTCLoggingSeverityNone
            }
            RTCSetMinDebugLogLevel(severity)
        }

        RTCInitFieldTrialDictionary(fieldTrials as Map<Any?, *>)
        RTCInitializeSSL()
        if (enableInternalTracer) {
            RTCSetupInternalTracer()
        }
    }

    actual fun dispose() {
        RTCShutdownInternalTracer()
        RTCCleanupSSL()
        coroutineScopeRef.value?.cancel()
        coroutineScopeRef.value = null
    }
}

private val coroutineScopeRef = AtomicReference<CoroutineScope?>(null)

actual val coroutineScope: CoroutineScope
    get() {
        val scope = coroutineScopeRef.value
        check(scope != null) { "WebRTC KMM is not initialized." }
        return scope
    }

private class IosMainScope : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()
}
