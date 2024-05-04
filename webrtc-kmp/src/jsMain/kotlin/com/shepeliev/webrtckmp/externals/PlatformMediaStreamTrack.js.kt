package com.shepeliev.webrtckmp.externals

import com.shepeliev.webrtckmp.MediaTrackConstraints
import com.shepeliev.webrtckmp.internal.asCommon
import org.w3c.dom.mediacapture.MediaStreamTrack

internal actual fun PlatformMediaStreamTrack.getConstraints(): MediaTrackConstraints {
    return (this as MediaStreamTrack).getConstraints().asCommon()
}
