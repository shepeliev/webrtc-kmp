package com.shepeliev.webrtckmp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.webrtc.EglBase

actual inline fun runTest(crossinline block: suspend () -> Unit) = runBlocking { block() }

actual fun initializeWebRtc() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    WebRtcKmp.initialize(context, EglBase.create())
}

actual fun disposeWebRtc() {
    WebRtcKmp.dispose()
}
