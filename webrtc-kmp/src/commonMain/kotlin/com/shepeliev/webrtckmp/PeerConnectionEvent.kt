package com.shepeliev.webrtckmp

internal sealed interface PeerConnectionEvent {
    data class SignalingStateChange(val state: SignalingState) : PeerConnectionEvent
    data class IceConnectionStateChange(val state: IceConnectionState) : PeerConnectionEvent
    data class StandardizedIceConnectionChange(val state: IceConnectionState) : PeerConnectionEvent
    data class ConnectionStateChange(val state: PeerConnectionState) : PeerConnectionEvent
    data class IceGatheringStateChange(val state: IceGatheringState) : PeerConnectionEvent
    data class NewIceCandidate(val candidate: IceCandidate) : PeerConnectionEvent
    data class RemovedIceCandidates(val candidates: List<IceCandidate>) : PeerConnectionEvent
    data class NewDataChannel(val dataChannel: DataChannel) : PeerConnectionEvent
    data class RemoveTrack(val rtpReceiver: RtpReceiver) : PeerConnectionEvent
    object NegotiationNeeded : PeerConnectionEvent
    data class Track(val trackEvent: TrackEvent) : PeerConnectionEvent
}
