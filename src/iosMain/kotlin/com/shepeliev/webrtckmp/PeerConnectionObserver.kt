package com.shepeliev.webrtckmp

import WebRTC.RTCAudioTrack
import WebRTC.RTCDataChannel
import WebRTC.RTCIceCandidate
import WebRTC.RTCIceConnectionState
import WebRTC.RTCIceGatheringState
import WebRTC.RTCMediaStream
import WebRTC.RTCPeerConnection
import WebRTC.RTCPeerConnectionDelegateProtocol
import WebRTC.RTCPeerConnectionState
import WebRTC.RTCRtpMediaType
import WebRTC.RTCRtpReceiver
import WebRTC.RTCRtpTransceiver
import WebRTC.RTCSignalingState
import WebRTC.RTCVideoTrack
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import platform.darwin.NSObject
import kotlin.native.concurrent.freeze

internal class PeerConnectionObserver(
    private val events: PeerConnectionEvents,
) : NSObject(), RTCPeerConnectionDelegateProtocol {

    private val scope = MainScope()

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeSignalingState: RTCSignalingState
    ) {
        scope.launch {
            events.onSignalingStateChange.emit(rtcSignalingStateAsCommon(didChangeSignalingState))
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
        scope.launch {
            events.onIceConnectionStateChange.emit(
                rtcIceConnectionStateAsCommon(didChangeIceConnectionState)
            )
        }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeIceGatheringState: RTCIceGatheringState
    ) {
        scope.launch {
            events.onIceGatheringStateChange.emit(
                rtcIceGatheringStateAsCommon(didChangeIceGatheringState)
            )
        }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didGenerateIceCandidate: RTCIceCandidate
    ) {
        scope.launch {
            events.onIceCandidate.emit(IceCandidate(didGenerateIceCandidate))
        }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveIceCandidates: List<*>
    ) {
        val candidates = didRemoveIceCandidates.map { IceCandidate(it as RTCIceCandidate) }
        scope.launch { events.onRemovedIceCandidates.emit(candidates) }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didOpenDataChannel: RTCDataChannel
    ) {
        scope.launch {
            events.onDataChannel.emit(DataChannel(didOpenDataChannel).freeze())
        }
    }

    @Suppress("CONFLICTING_OVERLOADS")
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeStandardizedIceConnectionState: RTCIceConnectionState
    ) {
        scope.launch {
            events.onStandardizedIceConnectionChange.emit(
                rtcIceConnectionStateAsCommon(didChangeStandardizedIceConnectionState)
            )
        }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeConnectionState: RTCPeerConnectionState
    ) {
        scope.launch {
            events.onConnectionStateChange.emit(
                rtcPeerConnectionStateAsCommon(didChangeConnectionState)
            )
            if (didChangeConnectionState == RTCPeerConnectionState.RTCPeerConnectionStateClosed) {
                scope.cancel()
            }
        }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveReceiver: RTCRtpReceiver
    ) {
        scope.launch { events.onRemoveTrack.emit(RtpReceiver(didRemoveReceiver)) }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didStartReceivingOnTransceiver: RTCRtpTransceiver
    ) {
        val sender = didStartReceivingOnTransceiver.sender
        val receiver = didStartReceivingOnTransceiver.receiver
        val track = when (didStartReceivingOnTransceiver.mediaType) {
            RTCRtpMediaType.RTCRtpMediaTypeAudio -> {
                AudioStreamTrack(receiver.track as RTCAudioTrack, remote = true)
            }

            RTCRtpMediaType.RTCRtpMediaTypeVideo -> {
                VideoStreamTrack(receiver.track as RTCVideoTrack, remote = true)
            }

            RTCRtpMediaType.RTCRtpMediaTypeData,
            RTCRtpMediaType.RTCRtpMediaTypeUnsupported -> null
        }
        val streams = sender.streamIds.map { id ->
            MediaStream("$id").apply { track?.also { addTrack(it) } }
        }
        val trackEvent = TrackEvent(
            receiver = RtpReceiver(didStartReceivingOnTransceiver.receiver),
            streams = streams,
            track = track,
            transceiver = RtpTransceiver(didStartReceivingOnTransceiver)
        ).freeze()
        scope.launch { events.onTrack.emit(trackEvent) }
    }

    override fun peerConnectionShouldNegotiate(peerConnection: RTCPeerConnection) {
        scope.launch { events.onNegotiationNeeded.emit(Unit) }
    }
}
