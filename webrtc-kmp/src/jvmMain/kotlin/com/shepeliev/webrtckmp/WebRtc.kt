@file:JvmName("WebRtcKmpJVM")

package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.PeerConnectionFactory
import dev.onvoid.webrtc.logging.Logging

actual object WebRtc {

    internal val peerConnectionFactory: PeerConnectionFactory = PeerConnectionFactory()

    private var builder = WebRtcBuilder()

    fun configureBuilder(block: WebRtcBuilder.() -> Unit = {}) {
        block(builder)
    }

    actual fun initialize() {
        initLogging()
    }

    private fun initLogging() {
        with(builder) {
            val fieldTrialsString = fieldTrials.entries
                .joinToString(separator = "/") { "${it.key}/${it.value}" }

            loggingSeverity?.also {
                Logging.addLogSink(it) { severity, message ->
                    println(message)
                }
            }
        }
    }

    actual fun dispose() {
        peerConnectionFactory.dispose()
    }
}

class WebRtcBuilder(
    var fieldTrials: Map<String, String> = emptyMap(),
    var loggingSeverity: Logging.Severity? = null,
)

@Deprecated(
    "Use WebRtc.initialize()",
    replaceWith = ReplaceWith("WebRtc.initialize()")
)
fun initializeWebRtc(
    build: WebRtcBuilder.() -> Unit = {}
) {
    WebRtc.configureBuilder(build)
    WebRtc.initialize()
}