package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.RTCRtpSendParameters
import dev.onvoid.webrtc.RTCRtpSender

actual class RtpSender internal constructor(
    val native: RTCRtpSender,
    track: MediaStreamTrack?
) {
    actual val id: String
        get() = native.track.id

    private var _track: MediaStreamTrack? = track
    actual val track: MediaStreamTrack? get() = _track

    actual var parameters: RtpParameters
        get() = RtpParameters(native.parameters)
        set(value) {
            native.parameters = RTCRtpSendParameters().apply {
                this.transactionId = value.transactionId
                this.codecs = value.codecs.map {
                    it.native
                }
            }
        }

    actual val dtmf: DtmfSender?
        get() = null

    actual suspend fun replaceTrack(track: MediaStreamTrack?) {
        native.replaceTrack((track as? MediaStreamTrackImpl)?.native)
        _track = track
    }
}
