package com.shepeliev.webrtckmp.internal

import com.shepeliev.webrtckmp.IceServer
import kotlin.js.Json
import kotlin.js.json

internal fun IceServer.toPlatform(): Json = json(
    "urls" to urls.toTypedArray(),
    "username" to username,
    "credential" to password
)
