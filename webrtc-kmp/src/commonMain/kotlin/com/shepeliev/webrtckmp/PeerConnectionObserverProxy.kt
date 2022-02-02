package com.shepeliev.webrtckmp

internal class PeerConnectionObserverProxy: PeerConnectionObserver {
    private val observers = mutableSetOf<PeerConnectionObserver>()

    fun addObserver(observer: PeerConnectionObserver) {
        observers += observer
    }

    fun removeObserver(observer: PeerConnectionObserver) {
        observers -= observer
    }

    override fun onSignalingStateChange(state: SignalingState) {
        observers.forEach { it.onSignalingStateChange(state) }
    }

    override fun onIceConnectionStateChange(state: IceConnectionState) {
        observers.forEach { it.onIceConnectionStateChange(state) }
    }

    override fun onStandardizedIceConnectionChange(state: IceConnectionState) {
        observers.forEach { it.onStandardizedIceConnectionChange(state) }
    }

    override fun onConnectionStateChange(state: PeerConnectionState) {
        observers.forEach { it.onConnectionStateChange(state) }
    }

    override fun onIceGatheringStateChange(state: IceGatheringState) {
        observers.forEach { it.onIceGatheringStateChange(state) }
    }

    override fun onIceCandidate(candidate: IceCandidate) {
        observers.forEach { it.onIceCandidate(candidate) }
    }

    override fun onRemovedIceCandidates(candidates: List<IceCandidate>) {
        observers.forEach { it.onRemovedIceCandidates(candidates) }
    }

    override fun onDataChannel(dataChannel: DataChannel) {
        observers.forEach { it.onDataChannel(dataChannel) }
    }

    override fun onRemoveTrack(rtpReceiver: RtpReceiver) {
        observers.forEach { it.onRemoveTrack(rtpReceiver) }
    }

    override fun onNegotiationNeeded() {
        observers.forEach { it.onNegotiationNeeded() }
    }

    override fun onTrack(trackEvent: TrackEvent) {
        observers.forEach { it.onTrack(trackEvent) }
    }
}
