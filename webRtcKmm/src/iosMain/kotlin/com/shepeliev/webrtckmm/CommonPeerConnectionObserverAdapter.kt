package com.shepeliev.webrtckmm

import cocoapods.GoogleWebRTC.RTCDataChannel
import cocoapods.GoogleWebRTC.RTCIceCandidate
import cocoapods.GoogleWebRTC.RTCIceConnectionState
import cocoapods.GoogleWebRTC.RTCIceGatheringState
import cocoapods.GoogleWebRTC.RTCMediaStream
import cocoapods.GoogleWebRTC.RTCPeerConnection
import cocoapods.GoogleWebRTC.RTCPeerConnectionDelegateProtocol
import cocoapods.GoogleWebRTC.RTCPeerConnectionState
import cocoapods.GoogleWebRTC.RTCRtpReceiver
import cocoapods.GoogleWebRTC.RTCRtpTransceiver
import cocoapods.GoogleWebRTC.RTCSignalingState
import platform.darwin.NSObject

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
internal class CommonPeerConnectionObserverAdapter(private val observer: PeerConnectionObserver) :
    NSObject(), RTCPeerConnectionDelegateProtocol {

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeSignalingState: RTCSignalingState
    ) {
        observer.onSignalingChange(rtcSignalingStateAsCommon(didChangeSignalingState))
    }

    @Suppress("CONFLICTING_OVERLOADS")
    override fun peerConnection(peerConnection: RTCPeerConnection, didAddStream: RTCMediaStream) {
        observer.onAddStream(MediaStream(didAddStream))
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeIceConnectionState: RTCIceConnectionState
    ) {
        observer.onIceConnectionChange(rtcIceConnectionStateAsCommon(didChangeIceConnectionState))
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeIceGatheringState: RTCIceGatheringState
    ) {
        observer.onIceGatheringChange(rtcIceGatheringStateAsCommon(didChangeIceGatheringState))
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didGenerateIceCandidate: RTCIceCandidate
    ) {
        observer.onIceCandidate(IceCandidate(didGenerateIceCandidate))
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveIceCandidates: List<*>
    ) {
        val candidates = didRemoveIceCandidates.map { IceCandidate(it as RTCIceCandidate) }
        observer.onIceCandidatesRemoved(candidates)
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didOpenDataChannel: RTCDataChannel
    ) {
        observer.onDataChannel(DataChannel(didOpenDataChannel))
    }

    override fun peerConnectionShouldNegotiate(peerConnection: RTCPeerConnection) {
        observer.onRenegotiationNeeded()
    }

    @Suppress("CONFLICTING_OVERLOADS")
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveStream: RTCMediaStream
    ) {
        observer.onRemoveStream(MediaStream(didRemoveStream))
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeConnectionState: RTCPeerConnectionState
    ) {
        observer.onConnectionChange(rtcPeerConnectionStateAsCommon(didChangeConnectionState))
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveReceiver: RTCRtpReceiver
    ) {
        observer.onRemoveTrack(RtpReceiver(didRemoveReceiver))
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didStartReceivingOnTransceiver: RTCRtpTransceiver
    ) {
        observer.onTrack(RtpTransceiver(didStartReceivingOnTransceiver))
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didAddReceiver: RTCRtpReceiver,
        streams: List<*>
    ) {
        observer.onAddTrack(
            RtpReceiver(didAddReceiver),
            streams.map { MediaStream(it as RTCMediaStream) }
        )
    }
}

internal fun rtcSignalingStateAsCommon(state: RTCSignalingState): SignalingState {
    return when (state) {
        RTCSignalingState.RTCSignalingStateStable -> SignalingState.Stable
        RTCSignalingState.RTCSignalingStateHaveLocalOffer -> SignalingState.HaveLocalOffer
        RTCSignalingState.RTCSignalingStateHaveLocalPrAnswer -> SignalingState.HaveLocalPranswer
        RTCSignalingState.RTCSignalingStateHaveRemoteOffer -> SignalingState.HaveRemoteOffer
        RTCSignalingState.RTCSignalingStateHaveRemotePrAnswer -> SignalingState.HaveRemotePranswer
        RTCSignalingState.RTCSignalingStateClosed -> SignalingState.Closed
    }
}

internal fun rtcIceConnectionStateAsCommon(state: RTCIceConnectionState): IceConnectionState {
    return when (state) {
        RTCIceConnectionState.RTCIceConnectionStateNew -> IceConnectionState.New
        RTCIceConnectionState.RTCIceConnectionStateChecking -> IceConnectionState.Checking
        RTCIceConnectionState.RTCIceConnectionStateConnected -> IceConnectionState.Connected
        RTCIceConnectionState.RTCIceConnectionStateCompleted -> IceConnectionState.Completed
        RTCIceConnectionState.RTCIceConnectionStateFailed -> IceConnectionState.Failed
        RTCIceConnectionState.RTCIceConnectionStateDisconnected -> IceConnectionState.Disconnected
        RTCIceConnectionState.RTCIceConnectionStateClosed -> IceConnectionState.Closed
        RTCIceConnectionState.RTCIceConnectionStateCount -> IceConnectionState.Count
    }
}

internal fun rtcPeerConnectionStateAsCommon(state: RTCPeerConnectionState): PeerConnectionState {
    return when (state) {
        RTCPeerConnectionState.RTCPeerConnectionStateNew -> PeerConnectionState.New
        RTCPeerConnectionState.RTCPeerConnectionStateConnecting -> PeerConnectionState.Connecting
        RTCPeerConnectionState.RTCPeerConnectionStateConnected -> PeerConnectionState.Connected
        RTCPeerConnectionState.RTCPeerConnectionStateDisconnected -> PeerConnectionState.Disconnected
        RTCPeerConnectionState.RTCPeerConnectionStateFailed -> PeerConnectionState.Failed
        RTCPeerConnectionState.RTCPeerConnectionStateClosed -> PeerConnectionState.Closed
    }
}

internal fun rtcIceGatheringStateAsCommon(state: RTCIceGatheringState): IceGatheringState {
    return when (state) {
        RTCIceGatheringState.RTCIceGatheringStateNew -> IceGatheringState.New
        RTCIceGatheringState.RTCIceGatheringStateGathering -> IceGatheringState.Gathering
        RTCIceGatheringState.RTCIceGatheringStateComplete -> IceGatheringState.Complete
    }
}
