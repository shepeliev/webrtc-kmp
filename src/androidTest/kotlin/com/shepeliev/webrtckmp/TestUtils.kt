package com.shepeliev.webrtckmp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory

actual inline fun runTest(crossinline block: suspend () -> Unit) = runBlocking { block() }

actual fun initialize() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    initializeWebRtc(context, EglBase.create())
}

actual fun disposeWebRtc() {
    peerConnectionFactory.dispose()
    PeerConnectionFactory.shutdownInternalTracer()
}
