@file:JvmName("WebRtcKmpJvm")

package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.PeerConnectionFactory
import dev.onvoid.webrtc.logging.Logging

actual object WebRtc {
    private var _peerConnectionFactory: PeerConnectionFactory? = null
    internal val peerConnectionFactory: PeerConnectionFactory
        get() {
            if (_peerConnectionFactory == null) initialize()
            return checkNotNull(_peerConnectionFactory)
        }

    private var builder = WebRtcBuilder()

    fun configureBuilder(block: WebRtcBuilder.() -> Unit = {}) {
        block(builder)
    }

    actual fun initialize() {
        check(_peerConnectionFactory == null) { "WebRtc already initialized." }
        builder.loggingSeverity?.also { Logging.logToDebug(it) }
        _peerConnectionFactory = builder.factoryBuilder?.let { nnFactoryBuilder ->
            PeerConnectionFactory().apply(nnFactoryBuilder)
        } ?: PeerConnectionFactory()
    }

    actual fun dispose() {
        if (_peerConnectionFactory == null) return
        _peerConnectionFactory?.dispose()
        _peerConnectionFactory = null
    }
}

class WebRtcBuilder(
    var factoryBuilder: (PeerConnectionFactory.() -> Unit)? = null,
    var loggingSeverity: Logging.Severity? = null,
)

@Deprecated(
    "Use WebRtc.initialize()",
    replaceWith = ReplaceWith("WebRtc.initialize()"),
)
fun initializeWebRtc(
    build: WebRtcBuilder.() -> Unit = {},
) {
    WebRtc.configureBuilder(build)
    WebRtc.initialize()
}
