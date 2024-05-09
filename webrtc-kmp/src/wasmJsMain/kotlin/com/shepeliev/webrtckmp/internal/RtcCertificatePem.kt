package com.shepeliev.webrtckmp.internal

import com.shepeliev.webrtckmp.RtcCertificatePem
import com.shepeliev.webrtckmp.externals.RTCCertificate

internal external interface JsRTCCertificate : RTCCertificate, JsAny

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal fun RtcCertificatePem.toWasmJs(): JsRTCCertificate = js as JsRTCCertificate
