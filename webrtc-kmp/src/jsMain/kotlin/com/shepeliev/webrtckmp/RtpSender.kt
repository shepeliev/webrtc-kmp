package com.shepeliev.webrtckmp

actual class RtpSender(val native: RTCRtpSender) {
    actual val id: String
        get() = track?.id ?: ""

    actual val track: MediaStreamTrack?
        get() = native.track?.asCommon()

    actual var parameters: RtpParameters
        get() = RtpParameters(native.getParameters())
        set(value) = native.setParameters(value.js)

    actual val dtmf: DtmfSender?
        get() = native.dtmf?.let { DtmfSender(it) }

    actual fun replaceTrack(track: MediaStreamTrack?) {
        native.replaceTrack((track as? MediaStreamTrackImpl)?.native)
    }

    actual fun getCapabilities(kind: MediaStreamTrackKind): RtpCapabilities? {
        require(kind in listOf(MediaStreamTrackKind.Audio, MediaStreamTrackKind.Video)) {
            "Unsupported track kind: $kind"
        }

        native.getCapabilities(kind.asNative()).let {
            return RtpCapabilities(it, kind)
        }
    }
}
