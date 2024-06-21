package com.shepeliev.webrtckmp.internal

import com.shepeliev.webrtckmp.RtcConfiguration
import com.shepeliev.webrtckmp.externals.RTCPeerConnectionConfiguration

internal actual fun RtcConfiguration.toPlatform(): RTCPeerConnectionConfiguration = toWasmJs()

internal fun RtcConfiguration.toWasmJs(): WasmRtcConfiguration {
    return createRtcConfiguration(
        bundlePolicy.toStringValue().toJsString(),
        iceCandidatePoolSize,
        iceServers.map { it.toWasmJs() }.toJsArray(),
        iceTransportPolicy.toStringValue().toJsString(),
        rtcpMuxPolicy.toStringValue().toJsString(),
        certificates?.map { it.toWasmJs() }?.toJsArray()
    ).apply {
        if (certificates?.isNotEmpty() == true) {
            certificates.map { it.toWasmJs() }.toJsArray()
        }
    }
}

@Suppress("UNUSED_PARAMETER")
private fun createRtcConfiguration(
    bundlePolicy: JsString,
    iceCandidatePoolSize: Int,
    iceServers: JsArray<JsIceServer>,
    iceTransportPolicy: JsString,
    rtcpMuxPolicy: JsString,
    certificates: JsArray<JsRTCCertificate>?,
): WasmRtcConfiguration = js(
    """
    ({
        bundlePolicy: bundlePolicy,
        iceCandidatePoolSize: iceCandidatePoolSize,
        iceServers: iceServers,
        iceTransportPolicy: iceTransportPolicy,
        rtcpMuxPolicy: rtcpMuxPolicy,
        certificates: certificates || undefined
    })
"""
)
