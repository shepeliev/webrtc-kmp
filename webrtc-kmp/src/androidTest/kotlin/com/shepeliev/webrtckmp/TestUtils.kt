package com.shepeliev.webrtckmp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.webrtc.EglBase

actual inline fun runTest(
    timeout: Long,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    runBlocking {
        withTimeout(timeout) { block() }
    }
}

actual fun initializeTestWebRtc() {
    if (_applicationContext != null) return
    val context = ApplicationProvider.getApplicationContext<Context>()
    initializeWebRtc(context, EglBase.create())
}

actual val currentPlatform: Platform = Platform.Android
