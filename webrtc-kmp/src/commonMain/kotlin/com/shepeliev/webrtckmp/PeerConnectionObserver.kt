package com.shepeliev.webrtckmp

internal interface PeerConnectionObserver {
    fun onSignalingStateChange(state: SignalingState)
    fun onIceConnectionStateChange(state: IceConnectionState)
    fun onStandardizedIceConnectionChange(state: IceConnectionState)
    fun onConnectionStateChange(state: PeerConnectionState)
    fun onIceGatheringStateChange(state: IceGatheringState)
    fun onIceCandidate(candidate: IceCandidate)
    fun onRemovedIceCandidates(candidates: List<IceCandidate>)
    fun onDataChannel(dataChannel: DataChannel)
    fun onRemoveTrack(rtpReceiver: RtpReceiver)
    fun onNegotiationNeeded()
    fun onTrack(trackEvent: TrackEvent)
}
