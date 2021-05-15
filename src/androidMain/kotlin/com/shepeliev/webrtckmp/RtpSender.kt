package com.shepeliev.webrtckmp

import org.webrtc.RtpSender as NativeRtpSender

actual class RtpSender internal constructor(val native: NativeRtpSender) {
    actual val id: String
        get() = native.id()

    actual val track: MediaStreamTrack?
        get() = native.track()?.asCommon(remote = false)

    actual var parameters: RtpParameters
        get() = RtpParameters(native.parameters)
        set(value) {
            native.parameters = value.native
        }

    actual val dtmf: DtmfSender?
        get() = native.dtmf()?.let { DtmfSender(it) }

    actual suspend fun replaceTrack(track: MediaStreamTrack?) {
        native.setTrack(track?.native, true)
    }
}
