package com.shepeliev.webrtckmp.externals

import kotlin.js.JsName

@JsName("MediaStream")
external interface PlatformMediaStream {
    val id: String
    fun addTrack(track: PlatformMediaStreamTrack)
    fun getTrackById(id: String): PlatformMediaStreamTrack?
    fun removeTrack(track: PlatformMediaStreamTrack)
}

internal expect fun PlatformMediaStream(): PlatformMediaStream
internal expect fun PlatformMediaStream.getTracks(): List<PlatformMediaStreamTrack>
