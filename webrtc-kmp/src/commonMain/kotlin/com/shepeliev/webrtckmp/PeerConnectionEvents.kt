package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.MutableSharedFlow

internal class PeerConnectionEvents internal constructor() {
    val onSignalingStateChange = MutableSharedFlow<SignalingState>()
    val onIceConnectionStateChange = MutableSharedFlow<IceConnectionState>()
    val onStandardizedIceConnectionChange = MutableSharedFlow<IceConnectionState>()
    val onConnectionStateChange = MutableSharedFlow<PeerConnectionState>()
    val onIceGatheringStateChange = MutableSharedFlow<IceGatheringState>()
    val onIceCandidate = MutableSharedFlow<IceCandidate>()
    val onRemovedIceCandidates = MutableSharedFlow<List<IceCandidate>>()
    val onDataChannel = MutableSharedFlow<DataChannel>()
    val onRemoveTrack = MutableSharedFlow<RtpReceiver>()
    val onNegotiationNeeded = MutableSharedFlow<Unit>()
    val onTrack = MutableSharedFlow<TrackEvent>()
}
