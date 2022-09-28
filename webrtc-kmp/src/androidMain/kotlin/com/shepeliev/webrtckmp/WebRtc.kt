@file:JvmName("WebRtcKmpAndroid")

package com.shepeliev.webrtckmp

import android.content.Context
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.Logging
import org.webrtc.PeerConnectionFactory

actual object WebRtc {

    private var _eglBase: EglBase? = null
    val rootEglBase: EglBase
        get() {
            if (_eglBase ==null) initialize()
            return checkNotNull(_eglBase)
        }

    private var _peerConnectionFactory: PeerConnectionFactory? = null
    internal val peerConnectionFactory: PeerConnectionFactory
        get() {
            if (_peerConnectionFactory == null) initialize()
            return checkNotNull(_peerConnectionFactory)
        }

    private var builder = WebRtcBuilder()

    fun configure(build: WebRtcBuilder.() -> Unit = {}) {
        build(builder)
    }

    actual fun initialize() {
        check(_peerConnectionFactory == null) { "WebRtc already initialized." }
        _eglBase = builder.eglBase ?: EglBase.create()
        initializePeerConnectionFactory()
        val pcfBuilder = builder.factoryBuilder ?: getDefaultPeerConnectionBuilder()
        _peerConnectionFactory = pcfBuilder.createPeerConnectionFactory()
    }

    private fun initializePeerConnectionFactory() {
        with(builder) {
            val fieldTrialsString = fieldTrials.entries
                .joinToString(separator = "/") { "${it.key}/${it.value}" }

            val initOptions = PeerConnectionFactory.InitializationOptions
                .builder(ApplicationContextHolder.context)
                .setFieldTrials(fieldTrialsString)
                .setEnableInternalTracer(enableInternalTracer)
                .createInitializationOptions()

            PeerConnectionFactory.initialize(initOptions)
            loggingSeverity?.also { Logging.enableLogToDebugOutput(it) }
        }
    }

    private fun getDefaultPeerConnectionBuilder(): PeerConnectionFactory.Builder {
        val videoEncoderFactory = DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, false)

        val videoDecoderFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)
        return PeerConnectionFactory.builder()
            .setVideoEncoderFactory(videoEncoderFactory)
            .setVideoDecoderFactory(videoDecoderFactory)
    }

    actual fun dispose() {
        if (_peerConnectionFactory == null) return

        _eglBase?.release()
        _eglBase = null

        _peerConnectionFactory?.dispose()
        _peerConnectionFactory = null

        PeerConnectionFactory.shutdownInternalTracer()
    }
}

class WebRtcBuilder(
    var factoryBuilder: PeerConnectionFactory.Builder? = null,
    var fieldTrials: Map<String, String> = emptyMap(),
    var enableInternalTracer: Boolean = false,
    var loggingSeverity: Logging.Severity? = null,
    var eglBase: EglBase? = null,
)

@Deprecated(
    "Use WebRtc.initialize()",
    replaceWith = ReplaceWith("WebRtc.initialize()")
)
fun initializeWebRtc(
    context: Context,
    eglBaseInstance: EglBase = EglBase.create(),
    build: WebRtcBuilder.() -> Unit = {}
) {
    WebRtc.configure(build)
    WebRtc.initialize()
}

@Deprecated(
    "Use WebRtc.rootEglBase",
    replaceWith = ReplaceWith("WebRtc.rootEglBase")
)
val eglBase: EglBase get() = WebRtc.rootEglBase

@Deprecated(
    "Use WebRtc.rootEglBase.eglBaseContext",
    replaceWith = ReplaceWith("WebRtc.rootEglBase.eglBaseContext")
)
val eglBaseContext: EglBase.Context
    get() = WebRtc.rootEglBase.eglBaseContext
