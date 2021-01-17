package com.shepeliev.webrtckmm

import com.shepeliev.webrtckmm.utils.toNative
import org.webrtc.PeerConnection as NativePeerConnection

actual class PeerConnection internal constructor(val native: NativePeerConnection) {
    actual fun addIceCandidate(candidate: IceCandidate) {
        native.addIceCandidate(candidate.toNative())
    }

    actual fun removeIceCandidates(candidates: List<IceCandidate>) {
        native.removeIceCandidates(candidates.map { it.toNative() }.toTypedArray())
    }
}
