package com.shepeliev.webrtckmp.util

import platform.Foundation.NSUUID

internal actual object UUID {
    actual fun randomUUID(): String = NSUUID().UUIDString
}
