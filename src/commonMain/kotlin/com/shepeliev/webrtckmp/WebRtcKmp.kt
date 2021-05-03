package com.shepeliev.webrtckmp

import kotlinx.coroutines.CoroutineScope

expect object WebRtcKmp {
    fun initialize(
        fieldTrials: Map<String, String> = emptyMap(),
        enableInternalTracer: Boolean = false,
        loggingSeverity: WebRtcLoggingSeverity = WebRtcLoggingSeverity.None,
    )

    fun dispose()
}

enum class WebRtcLoggingSeverity { Verbose, Info, Warning, Error, None }

internal expect val coroutineScope: CoroutineScope
