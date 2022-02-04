@file:JvmName("WebRtcKmpAndroid")

package com.shepeliev.webrtckmp

import android.content.Context
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.Logging
import org.webrtc.PeerConnectionFactory

@Deprecated("It will be removed in one of the future releases.")
actual object WebRtc {

    @Deprecated(
        message = "Use MediaDevices companion object.",
        replaceWith = ReplaceWith("MediaDevices")
    )
    actual val mediaDevices: MediaDevices = MediaDevices
}

fun initializeWebRtc(
    context: Context,
    eglBase: EglBase = EglBase.create(),
    build: WebRtcBuilder.() -> Unit = {}
) {
    _applicationContext = context
    build(webRtcBuilder)
}

internal var _applicationContext: Context? = null
internal val applicationContext: Context
    get() = _applicationContext
        ?: error("initializeWebRtc(applicationContext) must be called before invoking any WebRTC KMP API. ")

val peerConnectionFactory: PeerConnectionFactory by lazy {
    initializePeerConnectionFactory()
    val builder = webRtcBuilder.factoryBuilder ?: getDefaultPeerConnectionBuilder()
    builder.createPeerConnectionFactory()
}

private var _rootEglBase: EglBase? = null
val rootEglBase: EglBase
    get() {
        _rootEglBase = _rootEglBase ?: EglBase.create()
        return _rootEglBase!!
    }

@Deprecated("Use rootEglBase", replaceWith = ReplaceWith("rootEglBase"))
val eglBase: EglBase
    get() = rootEglBase

@Deprecated("Use rootEglBase.eglBaseContext.", replaceWith = ReplaceWith("rootEglBase.eglBaseContext"))
val eglBaseContext: EglBase.Context
    get() = eglBase.eglBaseContext

class WebRtcBuilder(
    var factoryBuilder: PeerConnectionFactory.Builder? = null,
    var fieldTrials: Map<String, String> = emptyMap(),
    var enableInternalTracer: Boolean = false,
    var loggingSeverity: Logging.Severity? = null
)

private val webRtcBuilder = WebRtcBuilder()

private fun initializePeerConnectionFactory() {
    with(webRtcBuilder) {
        val fieldTrialsString = fieldTrials.entries
            .joinToString(separator = "/") { "${it.key}/${it.value}" }

        val initOptions = PeerConnectionFactory.InitializationOptions
            .builder(applicationContext)
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
