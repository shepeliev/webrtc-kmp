package com.shepeliev.webrtckmp

import kotlin.js.Json
import kotlin.js.json

internal fun SessionDescription.asJs(): Json = json(
    "type" to type.name.lowercase(),
    "sdp" to sdp,
)

internal fun RTCSessionDescription.asCommon(): SessionDescription {
    val type = SessionDescriptionType.valueOf(
        this.type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    )

    return SessionDescription(type, sdp)
}
