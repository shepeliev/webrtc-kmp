package com.shepeliev.webrtckmp

import WebRTC.RTCIceConnectionState
import WebRTC.RTCIceGatheringState
import WebRTC.RTCPeerConnectionState
import WebRTC.RTCSignalingState

internal fun rtcSignalingStateAsCommon(state: RTCSignalingState): SignalingState {
    return when (state) {
        RTCSignalingState.RTCSignalingStateStable -> SignalingState.Stable
        RTCSignalingState.RTCSignalingStateHaveLocalOffer -> SignalingState.HaveLocalOffer
        RTCSignalingState.RTCSignalingStateHaveLocalPrAnswer -> SignalingState.HaveLocalPranswer
        RTCSignalingState.RTCSignalingStateHaveRemoteOffer -> SignalingState.HaveRemoteOffer
        RTCSignalingState.RTCSignalingStateHaveRemotePrAnswer -> SignalingState.HaveRemotePranswer
        RTCSignalingState.RTCSignalingStateClosed -> SignalingState.Closed
        else -> error("Unknown RTCSignalingState: $state")
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
        else -> error("Unknown RTCIceConnectionState: $state")
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
        else -> error("Unknown RTCPeerConnectionStateState: $state")
    }
}

internal fun rtcIceGatheringStateAsCommon(state: RTCIceGatheringState): IceGatheringState {
    return when (state) {
        RTCIceGatheringState.RTCIceGatheringStateNew -> IceGatheringState.New
        RTCIceGatheringState.RTCIceGatheringStateGathering -> IceGatheringState.Gathering
        RTCIceGatheringState.RTCIceGatheringStateComplete -> IceGatheringState.Complete
        else -> error("Unknown RTCIceGatheringState: $state")
    }
}
