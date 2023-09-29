package com.shepeliev.webrtckmp.util

import java.util.UUID

internal actual object UUID {
    actual fun randomUUID(): String = UUID.randomUUID().toString()
}
