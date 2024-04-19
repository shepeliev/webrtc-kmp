@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCValues
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

internal fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(bytes = this@toNSData.toCValues().ptr, length = size.toULong())
}

internal fun NSData.toByteArray(): ByteArray {
    return ByteArray(length.toInt()).apply {
        usePinned { memcpy(it.addressOf(0), bytes, length) }
    }
}
