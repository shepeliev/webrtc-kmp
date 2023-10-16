package com.shepeliev.webrtckmp

expect interface AudioTrack : MediaStreamTrack

@Deprecated("Use AudioTrack instead", ReplaceWith("AudioTrack"))
typealias AudioStreamTrack = AudioTrack
