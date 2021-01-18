package com.shepeliev.webrtckmm

import com.shepeliev.webrtckmm.utils.toNative
import org.webrtc.DataChannel as NativeDataChannel
import org.webrtc.PeerConnection as NativePeerConnection

actual class PeerConnection internal constructor(val native: NativePeerConnection) {
    actual fun createDataChannel(
        label: String,
        id: Int,
        ordered: Boolean,
        maxRetransmitTimeMs: Int,
        maxRetransmits: Int,
        protocol: String,
        negotiated: Boolean
    ): DataChannel? {
        val init = NativeDataChannel.Init().also {
            it.id = id
            it.ordered = ordered
            it.maxRetransmitTimeMs = maxRetransmitTimeMs
            it.maxRetransmits = maxRetransmits
            it.protocol = protocol
            it.negotiated = negotiated
        }
        return native.createDataChannel(label, init)?.let { DataChannel(it) }
    }

    actual fun addIceCandidate(candidate: IceCandidate) {
        native.addIceCandidate(candidate.toNative())
    }

    actual fun removeIceCandidates(candidates: List<IceCandidate>) {
        native.removeIceCandidates(candidates.map { it.toNative() }.toTypedArray())
    }
}
