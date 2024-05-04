package com.shepeliev.webrtckmp.internal

import com.shepeliev.webrtckmp.SessionDescription
import com.shepeliev.webrtckmp.toCanonicalString
import kotlin.js.json

internal fun SessionDescription.toPlatform() = json(
    "type" to type.toCanonicalString(),
    "sdp" to sdp
)
