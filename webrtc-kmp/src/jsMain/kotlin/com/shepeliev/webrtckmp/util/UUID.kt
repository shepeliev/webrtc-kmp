package com.shepeliev.webrtckmp.util

internal actual object UUID {
    actual fun randomUUID(): String = v4()
}
