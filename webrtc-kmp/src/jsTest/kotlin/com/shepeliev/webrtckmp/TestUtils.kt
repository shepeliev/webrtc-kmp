package com.shepeliev.webrtckmp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlinx.coroutines.withTimeout

@DelicateCoroutinesApi
actual inline fun runTest(
    timeout: Long,
    crossinline block: suspend CoroutineScope.() -> Unit
): dynamic {
    return GlobalScope.promise {
        runCatching { withTimeout(timeout) { block() } }
            .onFailure { it.log() }
            .exceptionOrNull()?.also { throw it }
    }.asDynamic()
}

fun Throwable.log() {
    console.error(this)
    cause?.let {
        console.error("Caused by:")
        it.log()
    }
}

actual fun setupMocks() {}
