package com.shepeliev.webrtckmp.externals

import com.shepeliev.webrtckmp.internal.toList
import org.w3c.dom.mediacapture.MediaStream

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual fun PlatformMediaStream.getTracks(): List<PlatformMediaStreamTrack> {
    return (this as MediaStream).getTracks().toList().filterNotNull().map { it as PlatformMediaStreamTrack }
}
