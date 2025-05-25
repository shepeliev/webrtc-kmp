package com.shepeliev.webrtckmp.internal

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.Promise

// TODO: remove after fixing in kotlinx.coroutines
// PR: https://github.com/Kotlin/kotlinx.coroutines/pull/4120
@Suppress("UNCHECKED_CAST")
internal suspend fun <T> Promise<JsAny?>.await(): T =
    suspendCancellableCoroutine { cont: CancellableContinuation<T> ->
        this@await.then(
            onFulfilled = {
                cont.resume(it as T)
                null
            },
            onRejected = {
                val exception = it.toThrowableOrNull() ?: Exception("Non-Kotlin exception $it")
                cont.resumeWithException(exception)
                null
            },
        )
    }
