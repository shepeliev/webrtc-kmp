package com.shepeliev.webrtckmp

@Deprecated("It will be removed in one of the future releases.")
actual object WebRtc {

    @Deprecated(
        message = "Use MediaDevices companion object.",
        replaceWith = ReplaceWith("MediaDevices")
    )
    actual val mediaDevices: MediaDevices = MediaDevices
}
