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
import org.webrtc.SurfaceTextureHelper
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
private var surfaceTextureHelperInternal: SurfaceTextureHelper? = null

val WebRtcKmp.eglBase: EglBase
    get() {
        checkNotNull(eglBaseInternal) { NOT_INITIALIZED_ERROR_MESSAGE }
        return eglBaseInternal!!
    }

internal val WebRtcKmp.applicationContext: Context
    get() {
        checkNotNull(applicationContextInternal) { NOT_INITIALIZED_ERROR_MESSAGE }
        return applicationContextInternal!!
    }

internal val WebRtcKmp.surfaceTextureHelper: SurfaceTextureHelper
    get() {
        checkNotNull(surfaceTextureHelperInternal) { NOT_INITIALIZED_ERROR_MESSAGE }
        return surfaceTextureHelperInternal!!
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
    createPeerConnectionFactory(peerConnectionFactoryBuilder)
    createSurfaceTextureHelper()
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

private fun createPeerConnectionFactory(peerConnectionFactoryBuilder: AndroidPeerConnectionFactory.Builder?) {
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

private fun createSurfaceTextureHelper() {
    surfaceTextureHelperInternal = SurfaceTextureHelper.create(
        "WebRTC.KMP.SurfaceTextureHelper",
        eglBaseInternal!!.eglBaseContext
    )
}

fun WebRtcKmp.dispose() {
    surfaceTextureHelperInternal?.dispose()
    surfaceTextureHelperInternal = null
    eglBaseInternal?.release()
    eglBaseInternal = null
    peerConnectionFactoryInternal?.native?.dispose()
    peerConnectionFactoryInternal = null
    mainScopeInternal?.cancel()
    mainScopeInternal = null
    applicationContextInternal = null
    AndroidPeerConnectionFactory.shutdownInternalTracer()
}
