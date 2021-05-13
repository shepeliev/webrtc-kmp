@file:JvmName("WebRtcKmpAndroid")

package com.shepeliev.webrtckmp

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.webrtc.EglBase
import org.webrtc.Logging
import org.webrtc.PeerConnectionFactory

actual object WebRtcKmp {
    actual val mainScope: CoroutineScope
        get() {
            check(mainScopeInternal != null) { NOT_INITIALIZED_ERROR_MESSAGE }
            return mainScopeInternal!!
        }
}

private var mainScopeInternal: CoroutineScope? = null
private var eglBaseInternal: EglBase? = null
private var applicationContextInternal: Context? = null

val WebRtcKmp.eglBase: EglBase
    get() {
        check(eglBaseInternal != null) { NOT_INITIALIZED_ERROR_MESSAGE }
        return eglBaseInternal!!
    }

internal val WebRtcKmp.applicationContext: Context
    get() {
        check(applicationContextInternal != null) { NOT_INITIALIZED_ERROR_MESSAGE }
        return applicationContextInternal!!
    }


fun WebRtcKmp.initialize(
    context: Context,
    eglBase: EglBase,
    fieldTrials: Map<String, String> = emptyMap(),
    enableInternalTracer: Boolean = false,
    loggingSeverity: Logging.Severity? = null
) {
    applicationContextInternal = context
    mainScopeInternal = MainScope()

    val fieldTrialsString = fieldTrials.entries.joinToString(separator = "/") {
        "${it.key}/${it.value}"
    }
    val initOptions = PeerConnectionFactory.InitializationOptions.builder(context)
        .setFieldTrials(fieldTrialsString)
        .setEnableInternalTracer(enableInternalTracer)
        .createInitializationOptions()
    PeerConnectionFactory.initialize(initOptions)

    loggingSeverity?.also { Logging.enableLogToDebugOutput(it) }

    eglBaseInternal = eglBase
}

fun WebRtcKmp.dispose() {
    PeerConnectionFactory.shutdownInternalTracer()
    eglBaseInternal?.release()
    eglBaseInternal = null
    mainScopeInternal?.cancel()
    mainScopeInternal = null
    applicationContextInternal = null
}
