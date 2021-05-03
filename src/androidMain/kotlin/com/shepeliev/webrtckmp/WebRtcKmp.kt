package com.shepeliev.webrtckmp

import com.shepeliev.webrtckmp.android.ApplicationContextProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.webrtc.Logging
import org.webrtc.PeerConnectionFactory

actual object WebRtcKmp {
    actual fun initialize(
        fieldTrials: Map<String, String>,
        enableInternalTracer: Boolean,
        loggingSeverity: WebRtcLoggingSeverity,
    ) {
        _coroutineScope = MainScope()

        if (loggingSeverity != WebRtcLoggingSeverity.None) {
            val severity = when(loggingSeverity) {
                WebRtcLoggingSeverity.Verbose -> Logging.Severity.LS_VERBOSE
                WebRtcLoggingSeverity.Info -> Logging.Severity.LS_INFO
                WebRtcLoggingSeverity.Warning -> Logging.Severity.LS_WARNING
                WebRtcLoggingSeverity.Error -> Logging.Severity.LS_ERROR
                WebRtcLoggingSeverity.None -> Logging.Severity.LS_NONE
            }
            Logging.enableLogToDebugOutput(severity)
        }

        val fieldTrialsString = fieldTrials.entries.joinToString(separator = "/") {
            "${it.key}/${it.value}"
        }
        val initOptions = PeerConnectionFactory.InitializationOptions.builder(
            ApplicationContextProvider.applicationContext
        )
            .setFieldTrials(fieldTrialsString)
            .setEnableInternalTracer(enableInternalTracer)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initOptions)
    }

    actual fun dispose() {
        PeerConnectionFactory.shutdownInternalTracer()
        _coroutineScope?.cancel()
        _coroutineScope = null
    }
}

private var _coroutineScope: CoroutineScope? = null;
actual val coroutineScope: CoroutineScope
get() {
    check(_coroutineScope != null) { "WebRTC KMM is not initialized!" }
    return _coroutineScope!!
}
