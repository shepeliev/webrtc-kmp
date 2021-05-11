package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class PeerConnectionEvents internal constructor() {

    internal val onSignalingStateInternal = MutableSharedFlow<SignalingState>()
    val onSignalingState: Flow<SignalingState> = onSignalingStateInternal.asSharedFlow()

    internal val onIceConnectionStateInternal = MutableSharedFlow<IceConnectionState>()
    val onIceConnectionState: Flow<IceConnectionState> = onIceConnectionStateInternal.asSharedFlow()

    internal val onStandardizedIceConnectionInternal = MutableSharedFlow<IceConnectionState>()
    val onStandardizedIceConnection: Flow<IceConnectionState> =
        onStandardizedIceConnectionInternal.asSharedFlow()

    internal val onConnectionStateInternal = MutableSharedFlow<PeerConnectionState>()
    val onConnectionState: Flow<PeerConnectionState> = onConnectionStateInternal.asSharedFlow()

    internal val onIceGatheringStateInternal = MutableSharedFlow<IceGatheringState>()
    val onIceGatheringState: Flow<IceGatheringState> = onIceGatheringStateInternal.asSharedFlow()

    internal val onIceCandidateInternal = MutableSharedFlow<IceCandidate>()
    val onIceCandidate: Flow<IceCandidate> = onIceCandidateInternal.asSharedFlow()

    internal val onRemovedIceCandidatesInternal = MutableSharedFlow<List<IceCandidate>>()
    val onRemovedIceCandidates: Flow<List<IceCandidate>> =
        onRemovedIceCandidatesInternal.asSharedFlow()

    internal val onDataChannelInternal = MutableSharedFlow<DataChannel>()
    val onDataChannel: Flow<DataChannel> = onDataChannelInternal.asSharedFlow()

    internal val onAddStreamInternal = MutableSharedFlow<MediaStream>()
    val onAddStream: Flow<MediaStream> = onAddStreamInternal.asSharedFlow()

    internal val onRemoveStreamInternal = MutableSharedFlow<MediaStream>()
    val onRemoveStream: Flow<MediaStream> = onRemoveStreamInternal.asSharedFlow()

    internal val onAddTrackInternal = MutableSharedFlow<Pair<RtpReceiver, List<MediaStream>>>()
    val onAddTrack: Flow<Pair<RtpReceiver, List<MediaStream>>> = onAddTrackInternal.asSharedFlow()

    internal val onRemoveTrackInternal = MutableSharedFlow<RtpReceiver>()
    val onRemoveTrack: Flow<RtpReceiver> = onRemoveTrackInternal.asSharedFlow()

    internal val onNegotiationNeededInternal = MutableSharedFlow<Unit>()
    val onNegotiationNeeded: Flow<Unit> = onNegotiationNeededInternal.asSharedFlow()
}
