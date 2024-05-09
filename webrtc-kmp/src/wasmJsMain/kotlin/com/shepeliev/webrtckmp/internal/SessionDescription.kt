package com.shepeliev.webrtckmp.internal

import com.shepeliev.webrtckmp.SessionDescription
import com.shepeliev.webrtckmp.externals.WasmRTCSessionDescription
import com.shepeliev.webrtckmp.toCanonicalString

internal fun SessionDescription.toWasmJs(): WasmRTCSessionDescription =
    createRTCSessionDescription(type.toCanonicalString(), sdp)

@Suppress("UNUSED_PARAMETER")
private fun createRTCSessionDescription(type: String, sdp: String): WasmRTCSessionDescription =
    js("({type: type, sdp: sdp})")
