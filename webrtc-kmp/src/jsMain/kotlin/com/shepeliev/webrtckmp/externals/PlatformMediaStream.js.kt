package com.shepeliev.webrtckmp.externals

import org.w3c.dom.mediacapture.MediaStream

internal actual fun PlatformMediaStream(): PlatformMediaStream {
    return MediaStream().unsafeCast<PlatformMediaStream>()
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual fun PlatformMediaStream.getTracks(): List<PlatformMediaStreamTrack> {
    return (this as MediaStream).getTracks().map { it as PlatformMediaStreamTrack }.toList()
}
