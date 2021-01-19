package com.shepeliev.webrtckmm

import org.webrtc.RtpSender as NativeRtpSender

actual class RtpSender(val native: NativeRtpSender) {
    actual val id: String
        get() = native.id()

    actual val track: MediaStreamTrack?
        get() = native.track()?.toCommon()

    actual var streams: List<String>
        get() = native.streams
        set(value) {
            native.streams = value
        }

    actual var parameters: RtpParameters
        get() = RtpParameters(native.parameters)
        set(value) {
            native.parameters = value.native
        }

    actual val dtmf: DtmfSender?
        get() = native.dtmf()?.let { DtmfSender(it) }

    actual fun setTrack(track: MediaStreamTrack?, takeOwnership: Boolean): Boolean {
        return native.setTrack((track as? BaseMediaStreamTrack)?.native, takeOwnership)
    }

    actual fun dispose() = native.dispose()
}

internal fun NativeRtpSender.toCommon() = RtpSender(this)
