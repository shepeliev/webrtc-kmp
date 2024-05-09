package com.shepeliev.webrtckmp.internal

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual fun Any.jsonStringify(): String {
    return JSON.stringify(this as JsAny)
}

private external object JSON {
    fun stringify(obj: JsAny): String
}
