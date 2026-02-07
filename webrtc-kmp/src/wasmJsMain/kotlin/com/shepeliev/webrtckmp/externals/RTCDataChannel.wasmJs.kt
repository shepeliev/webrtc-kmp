package com.shepeliev.webrtckmp.externals

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

/** Returns MessageEvent.data as Int8Array. Handles both string (TextEncoder) and ArrayBuffer. */
private fun messageEventDataToInt8Array(ev: WasmMessageEvent): Int8Array = js("(function(e){var d=e.data;if(typeof d==='string')return new Int8Array(new TextEncoder().encode(d).buffer);return new Int8Array(d);})(ev)")

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual val MessageEvent.data: ByteArray
    get() {
        val jsArray = messageEventDataToInt8Array(this as WasmMessageEvent)
        return ByteArray(jsArray.length) { jsArray[it] }
    }

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual fun RTCDataChannel.send(data: ByteArray) {
    val jsArray = Int8Array(data.size)
    data.forEachIndexed { index, byte -> jsArray[index] = byte }
    (this as WasmRTCDataChannel).send(jsArray)
}

@JsName("RTCDataChannel")
private external interface WasmRTCDataChannel : RTCDataChannel {
    fun send(data: Int8Array)
}

@JsName("MessageEvent")
private external interface WasmMessageEvent : MessageEvent {
    val data: ArrayBuffer
}
