package com.shepeliev.webrtckmp.externals

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual val MessageEvent.data: ByteArray
    get() {
        val jsArray = Int8Array((this as WasmMessageEvent).data)
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
