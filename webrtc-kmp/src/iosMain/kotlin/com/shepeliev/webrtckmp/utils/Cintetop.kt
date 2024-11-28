package com.shepeliev.webrtckmp.utils

import kotlinx.cinterop.CStructVar
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents

@OptIn(ExperimentalForeignApi::class)
internal inline fun <reified T : CStructVar> CValue<T>.copyContents(): T {
    lateinit var value: T
    this.useContents { value = this }
    return value
}
