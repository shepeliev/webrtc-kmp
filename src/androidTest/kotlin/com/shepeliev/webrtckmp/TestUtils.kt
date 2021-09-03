package com.shepeliev.webrtckmp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory

actual inline fun runTest(
    timeout: Long,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    runBlocking {
        withTimeout(timeout) {
            coroutineScope { block() }
        }
    }
}

actual fun initialize() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    initializeWebRtc(context, EglBase.create())
}

actual fun disposeWebRtc() {
    peerConnectionFactory.dispose()
    PeerConnectionFactory.shutdownInternalTracer()
}
