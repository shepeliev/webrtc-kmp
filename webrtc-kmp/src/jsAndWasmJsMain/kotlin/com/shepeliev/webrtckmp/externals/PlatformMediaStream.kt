package com.shepeliev.webrtckmp.externals

import kotlin.js.JsName

@JsName("MediaStream")
public external interface PlatformMediaStream {
    public val id: String

    public fun addTrack(track: PlatformMediaStreamTrack)

    public fun getTrackById(id: String): PlatformMediaStreamTrack?

    public fun removeTrack(track: PlatformMediaStreamTrack)
}

internal expect fun PlatformMediaStream(): PlatformMediaStream

internal expect fun PlatformMediaStream.getTracks(): List<PlatformMediaStreamTrack>
