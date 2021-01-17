package com.shepeliev.webrtckmm

import kotlin.jvm.JvmOverloads

data class MediaConstraints @JvmOverloads constructor(
    val mandatory: Map<String, String> = emptyMap(),
    val optional: Map<String, String> = emptyMap()
)
