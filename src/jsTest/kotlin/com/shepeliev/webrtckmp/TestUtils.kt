package com.shepeliev.webrtckmp

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

@DelicateCoroutinesApi
actual inline fun runTest(crossinline block: suspend () -> Unit) = GlobalScope
    .promise {
        try {
            block()
        } catch (e: dynamic) {
            (e as? Throwable)?.log()
            throw e
        }
    }.asDynamic()

fun Throwable.log() {
    console.error(this)
    cause?.let {
        console.error("Caused by:")
        it.log()
    }
}

actual fun initialize() {
}

actual fun disposeWebRtc() {
    // no implementation for JS
}
