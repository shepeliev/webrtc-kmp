package com.shepeliev.webrtckmp

import org.webrtc.RtpReceiver as NativeRtpReceiver

actual class RtpReceiver(val native: NativeRtpReceiver) {
    actual val id: String
        get() = native.id()

    actual val track: MediaStreamTrack?
        get() = native.track()?.asCommon()

    actual val parameters: RtpParameters
        get() = RtpParameters(native.parameters)

    actual fun setObserver(observer: RtpReceiverObserver) {
        native.SetObserver { observer.onFirstPacketReceived(it.asCommon()) }
    }

    actual fun dispose() {
        native.dispose()
    }
}

internal fun NativeRtpReceiver.asCommon() = RtpReceiver(this)
