package com.shepeliev.webrtckmp

import WebRTC.RTCDataChannel
import WebRTC.RTCIceCandidate
import WebRTC.RTCIceConnectionState
import WebRTC.RTCIceGatheringState
import WebRTC.RTCMediaStream
import WebRTC.RTCPeerConnection
import WebRTC.RTCPeerConnectionDelegateProtocol
import WebRTC.RTCPeerConnectionState
import WebRTC.RTCRtpReceiver
import WebRTC.RTCRtpTransceiver
import WebRTC.RTCSignalingState
import kotlinx.coroutines.launch
import platform.darwin.NSObject
import kotlin.native.concurrent.freeze

internal class PeerConnectionObserver(
    private val events: PeerConnectionEvents,
) : NSObject(), RTCPeerConnectionDelegateProtocol {

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeLocalCandidate: RTCIceCandidate,
        remoteCandidate: RTCIceCandidate,
        lastReceivedMs: Int,
        changeReason: String
    ) {
        // TODO not implemented
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeSignalingState: RTCSignalingState
    ) {
        WebRtcKmp.mainScope.launch {
            events.onSignalingStateInternal.emit(rtcSignalingStateAsCommon(didChangeSignalingState))
        }
    }

    @Suppress("CONFLICTING_OVERLOADS")
    override fun peerConnection(peerConnection: RTCPeerConnection, didAddStream: RTCMediaStream) {
        WebRtcKmp.mainScope.launch { events.onAddStreamInternal.emit(MediaStream(didAddStream)) }
    }

    @Suppress("CONFLICTING_OVERLOADS")
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveStream: RTCMediaStream
    ) {
        WebRtcKmp.mainScope.launch { events.onRemoveStreamInternal.emit(MediaStream(didRemoveStream)) }
    }

    @Suppress("CONFLICTING_OVERLOADS")
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeIceConnectionState: RTCIceConnectionState
    ) {
        WebRtcKmp.mainScope.launch {
            events.onIceConnectionStateInternal.emit(
                rtcIceConnectionStateAsCommon(didChangeIceConnectionState)
            )
        }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeIceGatheringState: RTCIceGatheringState
    ) {
        WebRtcKmp.mainScope.launch {
            events.onIceGatheringStateInternal.emit(
                rtcIceGatheringStateAsCommon(didChangeIceGatheringState)
            )
        }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didGenerateIceCandidate: RTCIceCandidate
    ) {
        WebRtcKmp.mainScope.launch {
            events.onIceCandidateInternal.emit(IceCandidate(didGenerateIceCandidate))
        }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveIceCandidates: List<*>
    ) {
        val candidates = didRemoveIceCandidates.map { IceCandidate(it as RTCIceCandidate) }
        WebRtcKmp.mainScope.launch { events.onRemovedIceCandidatesInternal.emit(candidates) }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didOpenDataChannel: RTCDataChannel
    ) {
        WebRtcKmp.mainScope.launch {
            events.onDataChannelInternal.emit(DataChannel(didOpenDataChannel).freeze())
        }
    }

    @Suppress("CONFLICTING_OVERLOADS")
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeStandardizedIceConnectionState: RTCIceConnectionState
    ) {
        WebRtcKmp.mainScope.launch {
            events.onStandardizedIceConnectionInternal.emit(
                rtcIceConnectionStateAsCommon(didChangeStandardizedIceConnectionState)
            )
        }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeConnectionState: RTCPeerConnectionState
    ) {
        WebRtcKmp.mainScope.launch {
            events.onConnectionStateInternal.emit(
                rtcPeerConnectionStateAsCommon(didChangeConnectionState)
            )
        }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveReceiver: RTCRtpReceiver
    ) {
        WebRtcKmp.mainScope.launch { events.onRemoveTrackInternal.emit(RtpReceiver(didRemoveReceiver)) }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didStartReceivingOnTransceiver: RTCRtpTransceiver
    ) {
        // TODO not implemented
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didAddReceiver: RTCRtpReceiver,
        streams: List<*>
    ) {
        WebRtcKmp.mainScope.launch {
            events.onAddTrackInternal.emit(
                Pair(
                    RtpReceiver(didAddReceiver),
                    streams.map { MediaStream(it as RTCMediaStream) }
                )
            )
        }
    }

    override fun peerConnectionShouldNegotiate(peerConnection: RTCPeerConnection) {
        WebRtcKmp.mainScope.launch { events.onNegotiationNeededInternal.emit(Unit) }
    }
}
