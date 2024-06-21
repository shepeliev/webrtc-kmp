package com.shepeliev.webrtckmp.externals

import com.shepeliev.webrtckmp.SessionDescription
import com.shepeliev.webrtckmp.SessionDescriptionType

external interface RTCSessionDescription {
    val type: String
    val sdp: String
}

internal fun RTCSessionDescription.toSessionDescription(): SessionDescription {
    val type = SessionDescriptionType.valueOf(
        this.type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    )

    return SessionDescription(type, sdp)
}
