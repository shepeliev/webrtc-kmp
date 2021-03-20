package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCMediaSource
import cocoapods.GoogleWebRTC.RTCSourceState

abstract class BaseMediaSource(): MediaSource {

    abstract val native: RTCMediaSource

    override val state: MediaSource.State
        get() = rtcMediaSourceStateAsCommon(native.state())
}

private fun rtcMediaSourceStateAsCommon(state: RTCSourceState): MediaSource.State {
    return when(state) {
        RTCSourceState.RTCSourceStateInitializing -> MediaSource.State.Initializing
        RTCSourceState.RTCSourceStateLive -> MediaSource.State.Live
        RTCSourceState.RTCSourceStateEnded -> MediaSource.State.Ended
        RTCSourceState.RTCSourceStateMuted -> MediaSource.State.Muted
    }
}
