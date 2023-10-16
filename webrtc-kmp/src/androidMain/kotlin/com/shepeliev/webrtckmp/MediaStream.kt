@file:JvmName("AndroidMediaStream")
package com.shepeliev.webrtckmp

actual fun MediaStream(tracks: List<MediaStreamTrack>, id: String): MediaStream = CommonMediaStream(tracks, id)
