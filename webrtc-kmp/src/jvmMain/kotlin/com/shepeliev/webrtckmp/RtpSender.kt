package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCRtcpParameters
import dev.onvoid.webrtc.RTCRtpCodecParameters
import dev.onvoid.webrtc.RTCRtpEncodingParameters
import dev.onvoid.webrtc.RTCRtpHeaderExtensionParameters
import dev.onvoid.webrtc.RTCRtpSendParameters
import dev.onvoid.webrtc.RTCRtpSender
import dev.onvoid.webrtc.media.MediaType

actual class RtpSender internal constructor(val native: RTCRtpSender, track: MediaStreamTrack?) {
    actual val id: String
        get() = TODO()

    private var _track: MediaStreamTrack? = track
    actual val track: MediaStreamTrack? get() = _track

    actual var parameters: RtpParameters
        get() = RtpParameters(native = native.parameters)
        set(value) {
            native.parameters = RTCRtpSendParameters().apply {
                codecs = value.codecs.map {
                    RTCRtpCodecParameters(
                        it.payloadType,
                        MediaType.valueOf(
                            it.mimeType?.substringBefore("/")?.uppercase()
                                ?: throw Exception("Unknown Media Type!"),
                        ),
                        it.mimeType?.substringAfterLast("/")?.lowercase(),
                        it.clockRate,
                        it.numChannels,
                        it.parameters,
                    )
                }
                encodings = value.encodings.map { params ->
                    RTCRtpEncodingParameters().apply {
                        active = params.active
                        params.ssrc?.let { ssrc = it }
                        params.maxFramerate?.let { maxFramerate = it.toDouble() }
                        params.scaleResolutionDownBy?.let { scaleResolutionDownBy = it }
                        params.maxBitrateBps?.let { maxBitrate = it }
                        params.minBitrateBps?.let { minBitrate = it }
                    }
                }
                transactionId = value.transactionId
                rtcp = RTCRtcpParameters(value.rtcp.cname, value.rtcp.reducedSize)
                headerExtensions = value.headerExtension.map {
                    RTCRtpHeaderExtensionParameters(it.uri, it.id, it.encrypted)
                }
            }
        }

    actual val dtmf: DtmfSender?
        get() = null

    actual suspend fun replaceTrack(track: MediaStreamTrack?) {
        track?.native?.let { nnNative ->
            native.replaceTrack(nnNative)
            _track = track
        }
    }
}
