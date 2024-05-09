package com.shepeliev.webrtckmp.externals

internal external interface RTCDataChannel {
    val id: Int
    val label: String
    val readyState: String
    val bufferedAmount: Long

    var onopen: (() -> Unit)?
    var onclose: (() -> Unit)?
    var onclosing: (() -> Unit)?
    var onerror: ((ErrorEvent) -> Unit)?
    var onmessage: ((MessageEvent) -> Unit)?

    fun close()
}

internal external interface RTCDataChannelOptions
internal external interface MessageEvent

internal expect fun RTCDataChannel.send(data: ByteArray)
internal expect val MessageEvent.data: ByteArray
