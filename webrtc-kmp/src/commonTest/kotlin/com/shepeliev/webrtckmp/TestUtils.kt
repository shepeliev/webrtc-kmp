package com.shepeliev.webrtckmp

import kotlinx.coroutines.CoroutineScope

expect fun runTest(timeout: Long = 30000, block: suspend CoroutineScope.() -> Unit)
