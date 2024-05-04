package com.shepeliev.webrtckmp.internal

internal fun <T : JsAny?> List<T>.toJsArray(): JsArray<T> =
    JsArray<T>().also { array -> forEach { jsArrayPush(array, it) } }

internal fun List<String>.toJsArray(): JsArray<JsString> =
    JsArray<JsString>().also { array -> forEach { jsArrayPush(array, it.toJsString()) } }

@Suppress("UNUSED_PARAMETER")
private fun <T : JsAny?> jsArrayPush(array: JsArray<T>, value: T) {
    js("array.push(value)")
}

internal fun <T : JsAny> JsArray<T>.toList(): List<T?> = (0..<length).map { get(it) }
