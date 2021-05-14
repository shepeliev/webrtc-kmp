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
        // this deprecated API should not longer be used
        // https://developer.mozilla.org/en-US/docs/Web/API/RTCPeerConnection/onaddstream
    }

    @Suppress("CONFLICTING_OVERLOADS")
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveStream: RTCMediaStream
    ) {
        // The removestream event has been removed from the WebRTC specification in favor of
        // the existing removetrack event on the remote MediaStream and the corresponding
        // MediaStream.onremovetrack event handler property of the remote MediaStream.
        // The RTCPeerConnection API is now track-based, so having zero tracks in the remote
        // stream is equivalent to the remote stream being removed and the old removestream event.
        // https://developer.mozilla.org/en-US/docs/Web/API/RTCPeerConnection/onremovestream
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
        val rtpReceiver = didAddReceiver.freeze()
        WebRtcKmp.mainScope.launch {
            events.onAddTrackInternal.emit(RtpReceiver(rtpReceiver))
        }
    }

    override fun peerConnectionShouldNegotiate(peerConnection: RTCPeerConnection) {
        WebRtcKmp.mainScope.launch { events.onNegotiationNeededInternal.emit(Unit) }
    }
}
