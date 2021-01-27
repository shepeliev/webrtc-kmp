package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCRtpMediaType
import cocoapods.GoogleWebRTC.RTCRtpReceiver
import cocoapods.GoogleWebRTC.RTCRtpReceiverDelegateProtocol
import platform.darwin.NSObject

actual class RtpReceiver(val native: RTCRtpReceiver) {
    actual val id: String
        get() = native.receiverId

    actual val track: MediaStreamTrack?
        get() = native.track()?.let { BaseMediaStreamTrack.createCommon(it) }

    actual val parameters: RtpParameters
        get() = RtpParameters(native.parameters)

    actual fun setObserver(observer: RtpReceiverObserver) {
        native.delegate = object : NSObject(), RTCRtpReceiverDelegateProtocol {
            override fun rtpReceiver(
                rtpReceiver: RTCRtpReceiver,
                didReceiveFirstPacketForMediaType: RTCRtpMediaType
            ) {
                observer.onFirstPacketReceived(
                    rtcRtpMediaTypeAsCommon(
                        didReceiveFirstPacketForMediaType
                    )
                )
            }

        }
    }

    actual fun dispose() {
        // not applicable
    }
}
