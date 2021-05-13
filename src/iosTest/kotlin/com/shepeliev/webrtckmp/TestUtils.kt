package com.shepeliev.webrtckmp

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import platform.Foundation.NSDate
import platform.Foundation.NSDefaultRunLoopMode
import platform.Foundation.NSLog
import platform.Foundation.NSRunLoop
import platform.Foundation.create
import platform.Foundation.runMode

actual inline fun runTest(crossinline block: suspend () -> Unit) {
    val exception = runBlocking {
        val testRun = MainScope().async {
            try {
                block()
            }   catch (e: Throwable) {
                return@async e
            }

            return@async null
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

actual fun initializeWebRtc() {
    WebRtcKmp.initialize()
}

actual fun disposeWebRtc() {
    WebRtcKmp.dispose()
}
