package com.shepeliev.webrtckmp

actual class RtpReceiver(val native: RTCRtpReceiver) {
    actual val id: String
        get() = native.track.id

    actual val track: MediaStreamTrack?
        get() = native.track.asCommon()

    actual val parameters: RtpParameters
        get() = RtpParameters(native.getParameters())

    actual fun getCapabilities(kind: MediaStreamTrackKind): RtpCapabilities? {
        require(kind in listOf(MediaStreamTrackKind.Audio, MediaStreamTrackKind.Video)) {
            "Unsupported track kind: $kind"
        }

        native.getCapabilities(kind.asNative()).let {
            return RtpCapabilities(it, kind)
        }
    }
}
