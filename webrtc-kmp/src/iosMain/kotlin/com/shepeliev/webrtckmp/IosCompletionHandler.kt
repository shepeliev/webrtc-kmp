@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import kotlinx.coroutines.CompletableDeferred
import platform.Foundation.NSError

internal suspend inline fun <T> T.await(function: T.(callback: (NSError?) -> Unit) -> Unit) {
    val deferred = CompletableDeferred<Unit>()

    val handler = { error: NSError? ->
        if (error == null) {
            deferred.complete(Unit)
        } else {
            deferred.completeExceptionally(RuntimeException(error.localizedDescription))
        }
    }

    function(handler)
    deferred.await()
}

internal suspend inline fun <T, reified R> T.awaitResult(function: T.(callback: (R?, NSError?) -> Unit) -> Unit): R {
    val deferred = CompletableDeferred<R?>()

    val handler = { result: R?, error: NSError? ->
        if (error == null) {
            deferred.complete(result)
        } else {
            deferred.completeExceptionally(RuntimeException(error.localizedDescription))
        }
    }

    function(handler)
    return deferred.await() as R
}
