package com.shepeliev.webrtckmp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

actual fun runTest(
    timeout: Long,
    block: suspend CoroutineScope.() -> Unit
) {
    runBlocking {
        withTimeout(timeout) {
            coroutineScope { block() }
        }
    }
}
