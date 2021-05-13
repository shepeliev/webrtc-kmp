@file:JvmName("WebRtcKmpAndroid")

package com.shepeliev.webrtckmp

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.Logging
import org.webrtc.PeerConnectionFactory as AndroidPeerConnectionFactory

actual object WebRtcKmp {
    actual val mainScope: CoroutineScope
        get() {
            check(mainScopeInternal != null) { NOT_INITIALIZED_ERROR_MESSAGE }
            return mainScopeInternal!!
        }

    internal actual val peerConnectionFactory: PeerConnectionFactory
        get() {
            check(peerConnectionFactoryInternal != null) { NOT_INITIALIZED_ERROR_MESSAGE }
            return peerConnectionFactoryInternal!!
        }
}

private var mainScopeInternal: CoroutineScope? = null
private var eglBaseInternal: EglBase? = null
private var applicationContextInternal: Context? = null
private var peerConnectionFactoryInternal: PeerConnectionFactory? = null

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
    peerConnectionFactoryBuilder: AndroidPeerConnectionFactory.Builder? = null,
    fieldTrials: Map<String, String> = emptyMap(),
    enableInternalTracer: Boolean = false,
    loggingSeverity: Logging.Severity? = null
) {
    applicationContextInternal = context
    mainScopeInternal = MainScope()
    initializePeerConnectionFactory(fieldTrials, enableInternalTracer, loggingSeverity)
    eglBaseInternal = eglBase
    buildPeerConnectionFactory(peerConnectionFactoryBuilder)
}

private fun initializePeerConnectionFactory(
    fieldTrials: Map<String, String>,
    enableInternalTracer: Boolean,
    loggingSeverity: Logging.Severity?
) {
    val fieldTrialsString = fieldTrials.entries
        .joinToString(separator = "/") { "${it.key}/${it.value}" }

    val initOptions = AndroidPeerConnectionFactory.InitializationOptions
        .builder(applicationContextInternal)
        .setFieldTrials(fieldTrialsString)
        .setEnableInternalTracer(enableInternalTracer)
        .createInitializationOptions()

    AndroidPeerConnectionFactory.initialize(initOptions)
    loggingSeverity?.also { Logging.enableLogToDebugOutput(it) }
}

private fun buildPeerConnectionFactory(peerConnectionFactoryBuilder: AndroidPeerConnectionFactory.Builder?) {
    val builder = peerConnectionFactoryBuilder ?: getDefaultPeerConnectionBuilder()
    val androidPeerConnectionFactory = builder.createPeerConnectionFactory()
    peerConnectionFactoryInternal = PeerConnectionFactory(androidPeerConnectionFactory)
}

private fun getDefaultPeerConnectionBuilder(): AndroidPeerConnectionFactory.Builder {
    val eglContext = eglBaseInternal?.eglBaseContext ?: error(NOT_INITIALIZED_ERROR_MESSAGE)
    val videoEncoderFactory = DefaultVideoEncoderFactory(eglContext, true, true)
    val videoDecoderFactory = DefaultVideoDecoderFactory(eglContext)

    return AndroidPeerConnectionFactory.builder()
        .setVideoEncoderFactory(videoEncoderFactory)
        .setVideoDecoderFactory(videoDecoderFactory)
}

fun WebRtcKmp.dispose() {
    peerConnectionFactoryInternal?.native?.dispose()
    AndroidPeerConnectionFactory.shutdownInternalTracer()
    peerConnectionFactoryInternal = null
    eglBaseInternal?.release()
    eglBaseInternal = null
    mainScopeInternal?.cancel()
    mainScopeInternal = null
    applicationContextInternal = null
}
