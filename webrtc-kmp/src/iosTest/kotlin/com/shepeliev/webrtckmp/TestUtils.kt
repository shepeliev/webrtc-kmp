package com.shepeliev.webrtckmp

import WebRTC.RTCCleanupSSL
import WebRTC.RTCShutdownInternalTracer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import platform.Foundation.NSDate
import platform.Foundation.NSDefaultRunLoopMode
import platform.Foundation.NSLog
import platform.Foundation.NSRunLoop
import platform.Foundation.create
import platform.Foundation.runMode

actual inline fun runTest(
    timeout: Long,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    val exception = runBlocking {
        val testRun = MainScope().async {
            runCatching { withTimeout(timeout) { block() } }.exceptionOrNull()
        }
        while (testRun.isActive) {
            NSRunLoop.mainRunLoop.runMode(
                NSDefaultRunLoopMode,
                beforeDate = NSDate.create(timeInterval = 1.0, sinceDate = NSDate())
            )
            yield()
        }
        testRun.await()
    }

    exception?.also {
        NSLog("$it")
        it.printStackTrace()
        throw it
    }
}

actual fun initialize() {
    initializeWebRtc()
}

actual fun disposeWebRtc() {
    RTCShutdownInternalTracer()
    RTCCleanupSSL()
}
