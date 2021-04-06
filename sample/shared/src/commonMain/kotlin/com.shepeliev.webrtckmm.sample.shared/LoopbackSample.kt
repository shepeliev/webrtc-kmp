package com.shepeliev.webrtckmm.sample.shared

import com.shepeliev.webrtckmm.IceConnectionState
import com.shepeliev.webrtckmm.IceServer
import com.shepeliev.webrtckmm.MediaDevices
import com.shepeliev.webrtckmm.MediaStream
import com.shepeliev.webrtckmm.MediaStreamTrack
import com.shepeliev.webrtckmm.PeerConnection
import com.shepeliev.webrtckmm.RtcConfiguration
import com.shepeliev.webrtckmm.SdpSemantics
import com.shepeliev.webrtckmm.VideoRenderer
import com.shepeliev.webrtckmm.VideoTrack
import com.shepeliev.webrtckmm.create
import com.shepeliev.webrtckmm.mediaConstraints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

interface LoopbackSampleListener {
    fun onLocalTrackAvailable()
    fun onRemoteTrackAvailable()
    fun onCallEstablished()
    fun onCallEnded()
    fun onError(description: String)
}

class LoopbackSample(private val listener: LoopbackSampleListener) :
    CoroutineScope by CommonMainScope() {

    private val tag = "LoopbackSample"

    private lateinit var localVideo: VideoRenderer
    private lateinit var remoteVideo: VideoRenderer

    private var userMedia: MediaStream? = null
    private var localPeerConnection: PeerConnection? = null
    private var remotePeerConnection: PeerConnection? = null

    fun setVideoViews(localVideo: VideoRenderer, remoteVideo: VideoRenderer) {
        this.localVideo = localVideo
        this.remoteVideo = remoteVideo
    }

    fun startCall() = launch {
        try {
            val config = RtcConfiguration(
                iceServers = listOf(IceServer(urls = listOf("stun:stun.l.google.com:19302"))),
                sdpSemantics = SdpSemantics.UnifiedPlan,
            )
            val loopbackConstraints = mediaConstraints {
                optional { "DtlsSrtpKeyAgreement" to "false" }
            }

            localPeerConnection = PeerConnection.create(config, loopbackConstraints)
            remotePeerConnection = PeerConnection.create(config, loopbackConstraints)

            with(localPeerConnection!!) {
                signalingStateFlow.onEach {
                    Log.d(tag, "Local PC signaling state $it")
                }.launchIn(this@LoopbackSample)
                iceGatheringStateFlow.onEach {
                    Log.d(tag, "Local PC ICE gathering state $it")
                }.launchIn(this@LoopbackSample)
                iceConnectionStateFlow.onEach {
                    Log.d(tag, "Local PC ICE connection state $it")
                    if (it == IceConnectionState.Connected) {
                        listener.onCallEstablished()
                    }
                    if (it == IceConnectionState.Disconnected) {
                        stopCall()
                    }
                }.launchIn(this@LoopbackSample)
                connectionStateFlow.onEach {
                    Log.d(tag, "Local PC PeerConnection state $it")
                }.launchIn(this@LoopbackSample)
                iceCandidateFlow.onEach {
                    Log.d(tag, "Local PC ICE candidate $it")
                    remotePeerConnection?.addIceCandidate(it)
                }.launchIn(this@LoopbackSample)
            }

            with(remotePeerConnection!!) {
                signalingStateFlow.onEach {
                    Log.d(tag, "Remote PC signaling state $it")
                }.launchIn(this@LoopbackSample)
                iceGatheringStateFlow.onEach {
                    Log.d(tag, "Remote PC ICE gathering state $it")
                }.launchIn(this@LoopbackSample)
                iceConnectionStateFlow.onEach {
                    Log.d(tag, "Remote PC ICE connection state $it")
                }.launchIn(this@LoopbackSample)
                connectionStateFlow.onEach {
                    Log.d(tag, "Remote PC PeerConnection state $it")
                }.launchIn(this@LoopbackSample)
                iceCandidateFlow.onEach {
                    Log.d(tag, "Remote PC ICE candidate $it")
                    localPeerConnection?.addIceCandidate(it)
                }.launchIn(this@LoopbackSample)
                addTrackFlow.onEach { (receiver, _) ->
                    Log.w(tag, "Remote PC on add track ${receiver.track}")
                    if (receiver.track?.kind == MediaStreamTrack.VIDEO_TRACK_KIND) {
                        val track = (receiver.track as VideoTrack)
                        track.addSink(remoteVideo)
                        listener.onRemoteTrackAvailable()
                    }
                }.launchIn(this@LoopbackSample)
            }

            userMedia = MediaDevices.getUserMedia(audio = true, video = true)
            userMedia!!.videoTrack()?.addSink(localVideo)
            listener.onLocalTrackAvailable()

            userMedia!!.audioTracks.forEach {
                localPeerConnection?.addTrack(
                    it,
                    listOf(userMedia!!.id)
                )
            }
            userMedia!!.videoTracks.forEach {
                localPeerConnection?.addTrack(
                    it,
                    listOf(userMedia!!.id)
                )
            }

            val offerConstraints = mediaConstraints {
                mandatory { "OfferToReceiveAudio" to "true" }
                mandatory { "OfferToReceiveVideo" to "true" }
            }
            val offer = localPeerConnection?.createOffer(offerConstraints)
            Log.d(tag, "$offer")
            localPeerConnection?.setLocalDescription(offer!!)

            remotePeerConnection?.setRemoteDescription(offer!!)
            val answer = remotePeerConnection?.createAnswer(offerConstraints)
            remotePeerConnection?.setLocalDescription(answer!!)

            localPeerConnection?.setRemoteDescription(answer!!)
        } catch (e: Throwable) {
            Log.e(tag, "Error", e)
            listener.onError("${e.message}")
            stopCall()
            return@launch
        }
    }

    fun stopCall() {
        localPeerConnection?.close()
        localPeerConnection = null
        remotePeerConnection?.close()
        remotePeerConnection = null
        listener.onCallEnded()
    }
}