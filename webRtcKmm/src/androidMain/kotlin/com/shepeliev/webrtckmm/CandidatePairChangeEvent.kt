package com.shepeliev.webrtckmm

import org.webrtc.CandidatePairChangeEvent as NativeCandidatePairChangeEvent

actual class CandidatePairChangeEvent internal constructor(val native: NativeCandidatePairChangeEvent) {
    actual val local: IceCandidate = native.local.asCommon()
    actual val remote: IceCandidate = native.remote.asCommon()
    actual val lastDataReceivedMs: Int =  native.lastDataReceivedMs
    actual val reason: String = native.reason
    actual val estimatedDisconnectedTimeMs: Int = native.estimatedDisconnectedTimeMs
}

internal fun NativeCandidatePairChangeEvent.asCommon() = CandidatePairChangeEvent(this)
