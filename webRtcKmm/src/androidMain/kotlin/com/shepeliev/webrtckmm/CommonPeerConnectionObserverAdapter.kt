package com.shepeliev.webrtckmm

import org.webrtc.PeerConnection
import org.webrtc.CandidatePairChangeEvent as NativeCandidatePairChangeEvent
import org.webrtc.DataChannel as NativeDataChannel
import org.webrtc.IceCandidate as NativeIceCandidate
import org.webrtc.MediaStream as NativeMediaStream
import org.webrtc.RtpReceiver as NativeRtpReceiver
import org.webrtc.RtpTransceiver as NativeRtpTransceiver

class CommonPeerConnectionObserverAdapter(private val observer: PeerConnectionObserver) :
    PeerConnection.Observer {

    override fun onSignalingChange(newState: PeerConnection.SignalingState) {
        observer.onSignalingChange(newState.asCommon())
    }

    override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {
        observer.onIceConnectionChange(newState.asCommon())
    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
        observer.onConnectionChange(newState.asCommon())
    }

    override fun onIceConnectionReceivingChange(receiving: Boolean) {
        observer.onIceConnectionReceivingChange(receiving)
    }

    override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) {
        observer.onIceGatheringChange(newState.asCommon())
    }

    override fun onIceCandidate(candidate: NativeIceCandidate) {
        observer.onIceCandidate(candidate.asCommon())
    }

    override fun onIceCandidatesRemoved(candidates: Array<out NativeIceCandidate>) {
        observer.onIceCandidatesRemoved(candidates.map { it.asCommon() })
    }

    override fun onSelectedCandidatePairChanged(event: NativeCandidatePairChangeEvent) {
        observer.onSelectedCandidatePairChanged(event.asCommon())
    }

    override fun onAddStream(stream: NativeMediaStream) {
        observer.onAddStream(MediaStream(stream))
    }

    override fun onRemoveStream(stream: NativeMediaStream) {
        observer.onRemoveStream(MediaStream(stream))
    }

    override fun onDataChannel(dataChannel: NativeDataChannel) {
        observer.onDataChannel(DataChannel(dataChannel))
    }

    override fun onRenegotiationNeeded() {
        observer.onRenegotiationNeeded()
    }

    override fun onAddTrack(receiver: NativeRtpReceiver, streams: Array<out NativeMediaStream>) {
        observer.onAddTrack(RtpReceiver(receiver), streams.map { MediaStream(it) })
    }

    override fun onTrack(transceiver: NativeRtpTransceiver) {
        observer.onTrack(RtpTransceiver(transceiver))
    }
}
