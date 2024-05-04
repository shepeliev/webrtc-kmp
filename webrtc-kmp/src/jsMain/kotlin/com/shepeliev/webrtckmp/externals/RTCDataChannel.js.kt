package com.shepeliev.webrtckmp.externals

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual val MessageEvent.data: ByteArray
    get() = Int8Array((this as JsMessageEvent).data).unsafeCast<ByteArray>()

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual fun RTCDataChannel.send(data: ByteArray) {
    (this as JsRTCDataChannel).send(data.unsafeCast<Int8Array>())
}

@JsName("RTCDataChannel")
private external interface JsRTCDataChannel : RTCDataChannel {
    fun send(data: Int8Array)
}

@JsName("MessageEvent")
private external interface JsMessageEvent : MessageEvent {
    val data: ArrayBuffer
}
