/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.shepeliev.apprtckmm.shared.rtcclient

import com.shepeliev.apprtckmm.shared.Log
import com.shepeliev.apprtckmm.shared.SignalingParameters
import com.shepeliev.webrtckmm.AudioTrack
import com.shepeliev.webrtckmm.CameraVideoCapturerException
import com.shepeliev.webrtckmm.ContinualGatheringPolicy
import com.shepeliev.webrtckmm.DataChannel
import com.shepeliev.webrtckmm.IceCandidate
import com.shepeliev.webrtckmm.IceConnectionState
import com.shepeliev.webrtckmm.KeyType
import com.shepeliev.webrtckmm.MediaConstraints
import com.shepeliev.webrtckmm.MediaDevices
import com.shepeliev.webrtckmm.MediaStreamTrack
import com.shepeliev.webrtckmm.PeerConnection
import com.shepeliev.webrtckmm.PeerConnectionState
import com.shepeliev.webrtckmm.RtcConfiguration
import com.shepeliev.webrtckmm.RtcPeerConnection
import com.shepeliev.webrtckmm.RtcStatsReport
import com.shepeliev.webrtckmm.RtcpMuxPolicy
import com.shepeliev.webrtckmm.RtpSender
import com.shepeliev.webrtckmm.SdpSemantics
import com.shepeliev.webrtckmm.SessionDescription
import com.shepeliev.webrtckmm.TcpCandidatePolicy
import com.shepeliev.webrtckmm.VideoStream
import com.shepeliev.webrtckmm.VideoTrack
import com.shepeliev.webrtckmm.mediaConstraints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

/**
 * Peer connection client implementation.
 *
 *
 * All public methods are routed to local looper thread.
 * All PeerConnectionEvents callbacks are invoked from the same looper thread.
 * This class is a singleton.
 */
class PeerConnectionClient(
    private val events: PeerConnectionEvents,
    coroutineContext: CoroutineContext = Dispatchers.Main
) {

    private var peerConnection: PeerConnection? = null
    private var isError = false
    private var signalingParameters: SignalingParameters? = null
    private var sdpMediaConstraints: MediaConstraints? = null

    // Queued remote ICE candidates are consumed only after both local and
    // remote descriptions are set. Similarly local ICE candidates are sent to
    // remote peer after both local and remote description are set.
    private var queuedRemoteCandidates: MutableList<IceCandidate>? = null
    private var isInitiator = false
    private var localDescription: SessionDescription? = null

    // enableVideo is set to true if video should be rendered and sent.
    private var renderVideo = true
    private var localVideoTrack: VideoTrack? = null
    private var remoteVideoTrack: VideoTrack? = null
    private var localVideoSender: RtpSender? = null

    // enableAudio is set to true if audio should be sent.
    private var enableAudio = true
    private var localAudioTrack: AudioTrack? = null
    private var dataChannel: DataChannel? = null

    private val scope = CoroutineScope(coroutineContext)

    suspend fun createPeerConnection(
        signalingParameters: SignalingParameters
    ) {
        this.signalingParameters = signalingParameters
        try {
            createMediaConstraintsInternal()
            createPeerConnectionInternal()
        } catch (e: Exception) {
            reportError("Failed to create peer connection: " + e.message)
            throw e
        }
    }

    fun close() {
        Log.d(TAG, "Closing peer connection.")
        dataChannel?.close()
        dataChannel = null
//        peerConnection?.getSenders()?.forEach { it.track?.stop() }
        peerConnection?.close()
        peerConnection = null
        events.onPeerConnectionClosed()
        scope.cancel()
    }

    private fun createMediaConstraintsInternal() {
        sdpMediaConstraints = mediaConstraints {
            mandatory { "OfferToReceiveAudio" to "true" }
            mandatory { "OfferToReceiveVideo" to "true" }
        }
    }

    private suspend fun createPeerConnectionInternal() {
        if (isError) {
            Log.e(TAG, "Peerconnection factory is not created")
            return
        }
        Log.d(TAG, "Create peer connection.")
        queuedRemoteCandidates = mutableListOf()
        val iceServers = signalingParameters!!.iceServers
        val rtcConfiguration = RtcConfiguration(
            iceServers = iceServers,
            tcpCandidatePolicy = TcpCandidatePolicy.Disabled,
            rtcpMuxPolicy = RtcpMuxPolicy.Require,
            continualGatheringPolicy = ContinualGatheringPolicy.GatherContinually,
            keyType = KeyType.ECDSA,
            sdpSemantics = SdpSemantics.UnifiedPlan
        )
        peerConnection = RtcPeerConnection(rtcConfiguration).apply {

            iceConnectionStateFlow.onEach {
                Log.d(TAG, "IceConnectionState: $it")
                when (it) {
                    IceConnectionState.Connected -> events.onIceConnected()
                    IceConnectionState.Disconnected -> events.onIceDisconnected()
                    IceConnectionState.Failed -> reportError("ICE connection failed.")
                    else -> {
                    }
                }
            }
                .launchIn(scope)

            connectionStateFlow.onEach {
                Log.d(TAG, "PeerConnectionState: $it")
                when (it) {
                    PeerConnectionState.Connected -> events.onConnected()
                    PeerConnectionState.Disconnected -> events.onDisconnected()
                    PeerConnectionState.Failed -> reportError("DTLS connection failed.")
                    else -> {
                    }

                }
            }
                .launchIn(scope)

            signalingStateFlow.onEach { Log.d(TAG, "SignalingState: $it") }.launchIn(scope)

            iceCandidateFlow.onEach {
                Log.d(TAG, "IceCandidate: $it")
                events.onIceCandidate(it)
            }.launchIn(scope)

            removedIceCandidatesFlow.onEach {
                Log.d(TAG, "Removed ICE candidates: $it")
                events.onIceCandidatesRemoved(it)
            }.launchIn(scope)

            iceGatheringStateFlow.onEach { Log.d(TAG, "IceGatheringState: $it") }.launchIn(scope)

//            coroutineScope.launch {
//                dataChannelFlow.collect {
//                    it.registerObserver(object : DataChannelObserver {
//                        override fun onMessage(buffer: DataChannelBuffer) {
//                            if (buffer.binary) {
//                                Log.d(TAG, "Received binary msg over $dataChannel")
//                                return
//                            }
//                            val bytes = buffer.data
//                            val strData = bytes.decodeToString()
//                            Log.d(TAG, "Got msg: $strData over $dataChannel")
//                        }
//
//                        override fun onBufferedAmountChange(previousAmount: Long) {
//                            Log.d(
//                                TAG,
//                                "Data channel buffered amount changed: ${dataChannel?.label}:  ${dataChannel?.state}"
//                            )
//                        }
//
//                        override fun onStateChange() {
//                            Log.d(
//                                TAG,
//                                "Data channel state changed: ${dataChannel?.label}: ${dataChannel?.state}"
//                            )
//                        }
//                    })
//                }
//            }
        }
        dataChannel = peerConnection!!.createDataChannel("ApprtcDemo data")
        isInitiator = false

        val stream = MediaDevices.getUserMedia(audio = true, video = true)
        events.onLocalVideoStream(stream)

        val mediaStreamLabels = listOf("ARDAMS")
        peerConnection!!.addTrack(stream.videoTracks.first(), mediaStreamLabels)
        // We can add the renderers right away because we don't need to wait for an
        // answer to get the remote track.
        remoteVideoTrack = getRemoteVideoTrack()
        remoteVideoTrack!!.enabled = renderVideo
        events.onRemoteVideoStream { remoteVideoTrack }
        peerConnection!!.addTrack(stream.audioTracks.first(), mediaStreamLabels)
        findVideoSender()
        Log.d(TAG, "Peer connection created.")
    }

    fun setAudioEnabled(enable: Boolean) {
        enableAudio = enable
        if (localAudioTrack != null) {
            localAudioTrack!!.enabled = enableAudio
        }
    }

    fun setVideoEnabled(enable: Boolean) {
        renderVideo = enable
        if (localVideoTrack != null) {
            localVideoTrack!!.enabled = renderVideo
        }
        if (remoteVideoTrack != null) {
            remoteVideoTrack!!.enabled = renderVideo
        }
    }

    suspend fun createOffer() {
        if (peerConnection != null && !isError) {
            Log.d(TAG, "PC Create OFFER")
            isInitiator = true
            val sdp = try {
                peerConnection!!.createOffer(sdpMediaConstraints!!)
            } catch (e: Throwable) {
                reportError("Create SDP failed ${e.message}")
                return
            }
            Log.d(TAG, "PC created OFFER")
            setSdp(sdp)
        }
    }

    suspend fun createAnswer() {
        if (peerConnection != null && !isError) {
            Log.d(TAG, "PC create ANSWER")
            isInitiator = false
            val sdp = try {
                peerConnection!!.createAnswer(sdpMediaConstraints!!)
            } catch (e: Throwable) {
                reportError("Create SDP failed ${e.message}")
                return
            }
            Log.d(TAG, "PC created ANSWER")
            setSdp(sdp)
        }
    }

    private suspend fun setSdp(description: SessionDescription) {
        if (localDescription != null) {
            reportError("Multiple SDP create.")
            return
        }
        localDescription = description
        if (peerConnection != null && !isError) {
            Log.d(TAG, "Set local SDP from " + description.type)

            try {
                peerConnection!!.setLocalDescription(description)
            } catch (e: Throwable) {
                reportError("Set SDP failed: ${e.message}")
                return
            }

            if (peerConnection == null || isError) {
                return
            }

            if (isInitiator) {
                // For offering peer connection we first create offer and set
                // local SDP, then after receiving answer set remote SDP.
                if (peerConnection!!.remoteDescription == null) {
                    // We've just set our local SDP so time to send it.
                    Log.d(TAG, "Local SDP set succesfully")
                    events.onLocalDescription(localDescription!!)
                } else {
                    // We've just set remote description, so drain remote
                    // and send local ICE candidates.
                    Log.d(TAG, "Remote SDP set succesfully")
                    drainCandidates()
                }
            } else {
                // For answering peer connection we set remote SDP and then
                // create answer and set local SDP.
                if (peerConnection!!.localDescription != null) {
                    // We've just set our local SDP so time to send it, drain
                    // remote and send local ICE candidates.
                    Log.d(TAG, "Local SDP set succesfully")
                    events.onLocalDescription(localDescription!!)
                    drainCandidates()
                } else {
                    // We've just set remote SDP - do nothing for now -
                    // answer will be created soon.
                    Log.d(TAG, "Remote SDP set succesfully")
                }
            }
        }
    }

    fun addRemoteIceCandidate(candidate: IceCandidate) {
        if (peerConnection != null && !isError) {
            if (queuedRemoteCandidates != null) {
                queuedRemoteCandidates!!.add(candidate)
            } else {
                peerConnection!!.addIceCandidate(candidate)
            }
        }
    }

    fun removeRemoteIceCandidates(candidates: List<IceCandidate>) {
        if (peerConnection == null || isError) {
            return
        }
        // Drain the queued remote candidates if there is any so that
        // they are processed in the proper order.
        drainCandidates()
        peerConnection!!.removeIceCandidates(candidates)
    }

    suspend fun setRemoteDescription(desc: SessionDescription) {
        if (peerConnection == null || isError) {
            return
        }
        peerConnection!!.setRemoteDescription(desc)
    }

    private fun reportError(errorMessage: String) {
        Log.e(TAG, "Peerconnection error: $errorMessage")
        if (!isError) {
            events.onPeerConnectionError(errorMessage)
            isError = true
        }
    }

    private fun findVideoSender() {
        for (sender in peerConnection!!.getSenders()) {
            if (sender.track != null) {
                val trackType = sender.track!!.kind
                if (trackType == MediaStreamTrack.VIDEO_TRACK_KIND) {
                    Log.d(TAG, "Found video sender.")
                    localVideoSender = sender
                }
            }
        }
    }

    // Returns the remote VideoTrack, assuming there is only one.
    private fun getRemoteVideoTrack(): VideoTrack? {
        for (transceiver in peerConnection!!.getTransceivers()) {
            val track = transceiver.receiver.track
            if (track is VideoTrack) {
                return track
            }
        }
        return null
    }

    private fun drainCandidates() {
        if (queuedRemoteCandidates != null) {
            Log.d(TAG, "Add " + queuedRemoteCandidates!!.size + " remote candidates")
            for (candidate in queuedRemoteCandidates!!) {
                peerConnection!!.addIceCandidate(candidate)
            }
            queuedRemoteCandidates = null
        }
    }

    suspend fun switchCamera() {
        try {
            MediaDevices.switchCamera()
        } catch (e: CameraVideoCapturerException) {
            Log.e(TAG, "Failed to switch camera. Error : ${e.message}")
        }
    }

    /**
     * Peer connection parameters.
     */
    data class DataChannelParameters(
        val ordered: Boolean,
        val maxRetransmitTimeMs: Int,
        val maxRetransmits: Int,
        val protocol: String,
        val negotiated: Boolean,
        val id: Int
    )

    /**
     * Peer connection parameters.
     */
    data class PeerConnectionParameters(
        val videoCallEnabled: Boolean,
        val loopback: Boolean,
        val tracing: Boolean,
        val videoWidth: Int,
        val videoHeight: Int,
        val videoFps: Int,
        val videoMaxBitrate: Int,
        val videoCodec: String,
        val videoCodecHwAcceleration: Boolean,
        val videoFlexfecEnabled: Boolean,
        val audioStartBitrate: Int,
        val audioCodec: String?,
        val noAudioProcessing: Boolean,
        val aecDump: Boolean,
        val saveInputAudioToFile: Boolean,
        val useOpenSLES: Boolean,
        val disableBuiltInAEC: Boolean,
        val disableBuiltInAGC: Boolean,
        val disableBuiltInNS: Boolean,
        val disableWebRtcAGCAndHPF: Boolean,
        val enableRtcEventLog: Boolean,
        val dataChannelParameters: DataChannelParameters?
    )

    companion object {
        const val VIDEO_TRACK_ID = "ARDAMSv0"
        const val AUDIO_TRACK_ID = "ARDAMSa0"
        const val VIDEO_TRACK_TYPE = "video"
        private const val TAG = "PCRTCClient"
        private const val VIDEO_CODEC_VP8 = "VP8"
        private const val VIDEO_CODEC_VP9 = "VP9"
        private const val VIDEO_CODEC_H264 = "H264"
        private const val VIDEO_CODEC_H264_BASELINE = "H264 Baseline"
        private const val VIDEO_CODEC_H264_HIGH = "H264 High"
        private const val AUDIO_CODEC_OPUS = "opus"
        private const val AUDIO_CODEC_ISAC = "ISAC"
        private const val VIDEO_CODEC_PARAM_START_BITRATE = "x-google-start-bitrate"
        private const val VIDEO_FLEXFEC_FIELDTRIAL =
            "WebRTC-FlexFEC-03-Advertised/Enabled/WebRTC-FlexFEC-03/Enabled/"
        private const val VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL = "WebRTC-IntelVP8/Enabled/"
        private const val DISABLE_WEBRTC_AGC_FIELDTRIAL =
            "WebRTC-Audio-MinimizeResamplingOnMobile/Enabled/"
        private const val AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate"
        private const val AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation"
        private const val AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl"
        private const val AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter"
        private const val AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression"
        private const val DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement"
        private const val HD_VIDEO_WIDTH = 1280
        private const val HD_VIDEO_HEIGHT = 720
        private const val BPS_IN_KBPS = 1000
        private const val RTCEVENTLOG_OUTPUT_DIR_NAME = "rtc_event_log"

        private fun getSdpVideoCodecName(parameters: PeerConnectionParameters): String {
            return when (parameters.videoCodec) {
                VIDEO_CODEC_VP8 -> VIDEO_CODEC_VP8
                VIDEO_CODEC_VP9 -> VIDEO_CODEC_VP9
                VIDEO_CODEC_H264_HIGH, VIDEO_CODEC_H264_BASELINE -> VIDEO_CODEC_H264
                else -> VIDEO_CODEC_VP8
            }
        }

        private fun getFieldTrials(peerConnectionParameters: PeerConnectionParameters): String {
            var fieldTrials = ""
            if (peerConnectionParameters.videoFlexfecEnabled) {
                fieldTrials += VIDEO_FLEXFEC_FIELDTRIAL
                Log.d(TAG, "Enable FlexFEC field trial.")
            }
            fieldTrials += VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL
            if (peerConnectionParameters.disableWebRtcAGCAndHPF) {
                fieldTrials += DISABLE_WEBRTC_AGC_FIELDTRIAL
                Log.d(TAG, "Disable WebRTC AGC field trial.")
            }
            return fieldTrials
        }

        /**
         * Peer connection events.
         */
        interface PeerConnectionEvents {
            /**
             * Callback fired once local SDP is created and set.
             */
            fun onLocalDescription(sdp: SessionDescription)

            /**
             * Callback fired once local Ice candidate is generated.
             */
            fun onIceCandidate(candidate: IceCandidate)

            /**
             * Callback fired once local ICE candidates are removed.
             */
            fun onIceCandidatesRemoved(candidates: List<IceCandidate>)

            /**
             * Callback fired once connection is established (IceConnectionState is
             * CONNECTED).
             */
            fun onIceConnected()

            /**
             * Callback fired once connection is disconnected (IceConnectionState is
             * DISCONNECTED).
             */
            fun onIceDisconnected()

            /**
             * Callback fired once DTLS connection is established (PeerConnectionState
             * is CONNECTED).
             */
            fun onConnected()

            /**
             * Callback fired once DTLS connection is disconnected (PeerConnectionState
             * is DISCONNECTED).
             */
            fun onDisconnected()

            /**
             * Callback fired once peer connection is closed.
             */
            fun onPeerConnectionClosed()

            /**
             * Callback fired once peer connection statistics is ready.
             */
            fun onPeerConnectionStatsReady(reports: Array<RtcStatsReport>)

            /**
             * Callback fired once peer connection error happened.
             */
            fun onPeerConnectionError(description: String?)

            fun onLocalVideoStream(stream: VideoStream)

            fun onRemoteVideoStream(stream: VideoStream)
        }
    }
}
