/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
@file:Suppress("DEPRECATION")
@file:JvmName("PeerConnectionClient")

package org.appspot.apprtc

import android.content.Context
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.util.Log
import com.shepeliev.webrtckmm.CandidatePairChangeEvent
import com.shepeliev.webrtckmm.ContinualGatheringPolicy
import com.shepeliev.webrtckmm.DataChannel
import com.shepeliev.webrtckmm.DataChannelBuffer
import com.shepeliev.webrtckmm.DataChannelObserver
import com.shepeliev.webrtckmm.IceCandidate
import com.shepeliev.webrtckmm.IceConnectionState
import com.shepeliev.webrtckmm.IceGatheringState
import com.shepeliev.webrtckmm.IceServer
import com.shepeliev.webrtckmm.KeyType
import com.shepeliev.webrtckmm.MediaStream
import com.shepeliev.webrtckmm.Options
import com.shepeliev.webrtckmm.PeerConnection
import com.shepeliev.webrtckmm.PeerConnectionFactory
import com.shepeliev.webrtckmm.PeerConnectionObserver
import com.shepeliev.webrtckmm.PeerConnectionState
import com.shepeliev.webrtckmm.RtcConfiguration
import com.shepeliev.webrtckmm.RtcpMuxPolicy
import com.shepeliev.webrtckmm.RtpReceiver
import com.shepeliev.webrtckmm.RtpTransceiver
import com.shepeliev.webrtckmm.SdpSemantics
import com.shepeliev.webrtckmm.SignalingState
import com.shepeliev.webrtckmm.TcpCandidatePolicy
import org.appspot.apprtc.AppRTCClient.SignalingParameters
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.CameraVideoCapturer
import org.webrtc.EglBase
import org.webrtc.Logging
import org.webrtc.MediaConstraints
import org.webrtc.RtpSender
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.StatsReport
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSink
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import org.webrtc.audio.AudioDeviceModule
import org.webrtc.audio.JavaAudioDeviceModule
import org.webrtc.audio.JavaAudioDeviceModule.AudioRecordErrorCallback
import org.webrtc.audio.JavaAudioDeviceModule.AudioRecordStateCallback
import org.webrtc.audio.JavaAudioDeviceModule.AudioTrackErrorCallback
import org.webrtc.audio.JavaAudioDeviceModule.AudioTrackStateCallback
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.regex.Pattern
import org.webrtc.DataChannel as WebRtcDataChannel
import org.webrtc.PeerConnectionFactory as WebRtcPeerConnectionFactory

/**
 * Peer connection client implementation.
 *
 *
 * All public methods are routed to local looper thread.
 * All PeerConnectionEvents callbacks are invoked from the same looper thread.
 * This class is a singleton.
 */
class PeerConnectionClient(
    private val appContext: Context?,
    private val rootEglBase: EglBase,
    private val peerConnectionParameters: PeerConnectionParameters,
    private val events: PeerConnectionEvents
) {
    private val pcObserver: PCObserver = PCObserver()
    private val sdpObserver: SDPObserver = SDPObserver()
    private val statsTimer = Timer()
    private var factory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var audioSource: AudioSource? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var videoSource: VideoSource? = null
    private var preferIsac = false
    private var videoCapturerStopped = false
    private var isError = false
    private var localRender: VideoSink? = null
    private var remoteSinks: List<VideoSink>? = null
    private var signalingParameters: SignalingParameters? = null
    private var videoWidth = 0
    private var videoHeight = 0
    private var videoFps = 0
    private var audioConstraints: MediaConstraints? = null
    private var sdpMediaConstraints: MediaConstraints? = null

    // Queued remote ICE candidates are consumed only after both local and
    // remote descriptions are set. Similarly local ICE candidates are sent to
    // remote peer after both local and remote description are set.
    private var queuedRemoteCandidates: MutableList<IceCandidate>? = null
    private var isInitiator = false
    private var localDescription // either offer or answer description
        : SessionDescription? = null
    private var videoCapturer: VideoCapturer? = null

    // enableVideo is set to true if video should be rendered and sent.
    private var renderVideo = true
    private var localVideoTrack: VideoTrack? = null
    private var remoteVideoTrack: VideoTrack? = null
    private var localVideoSender: RtpSender? = null

    // enableAudio is set to true if audio should be sent.
    private var enableAudio = true
    private var localAudioTrack: AudioTrack? = null
    private var dataChannel: DataChannel? = null
    private val dataChannelEnabled: Boolean = peerConnectionParameters.dataChannelParameters != null

    // Enable RtcEventLog.
    private var rtcEventLog: RtcEventLog? = null

    // Implements the WebRtcAudioRecordSamplesReadyCallback interface and writes
    // recorded audio samples to an output file.
    private var saveRecordedAudioToFile: RecordedAudioToFileController? = null

    private val isVideoCallEnabled: Boolean
        get() = peerConnectionParameters.videoCallEnabled && videoCapturer != null

    val isHDVideo: Boolean
        get() = isVideoCallEnabled && videoWidth * videoHeight >= 1280 * 720

    // TODO(sakal): getStats is deprecated.
    private val stats: Unit
        get() {
            if (peerConnection == null || isError) {
                return
            }
            val success = peerConnection!!.native.getStats({ reports ->
                events.onPeerConnectionStatsReady(reports)
            }, null)
            if (!success) {
                Log.e(TAG, "getStats() returns false!")
            }
        }

    /**
     * Create a PeerConnectionClient with the specified parameters. PeerConnectionClient takes
     * ownership of |eglBase|.
     */
    init {
        Log.d(TAG, "Preferred video codec: " + getSdpVideoCodecName(peerConnectionParameters))
        val fieldTrials = getFieldTrials(peerConnectionParameters)
        executor.execute {
            Log.d(TAG, "Initialize WebRTC. Field trials: $fieldTrials")
            PeerConnectionFactory.initialize(appContext, fieldTrials, true)
        }
    }


    /**
     * This function should only be called once.
     */
    fun createPeerConnectionFactory(options: Options?) {
        check(factory == null) { "PeerConnectionFactory has already been constructed" }
        executor.execute { createPeerConnectionFactoryInternal(options) }
    }

    fun createPeerConnection(
        localRender: VideoSink?,
        remoteSink: VideoSink?,
        videoCapturer: VideoCapturer?,
        signalingParameters: SignalingParameters?
    ) {
        if (peerConnectionParameters.videoCallEnabled && videoCapturer == null) {
            Log.w(TAG, "Video call enabled but no video capturer provided.")
        }
        val remoteSinks = remoteSink?.let { listOf(remoteSink) } ?: emptyList()
        createPeerConnection(localRender, remoteSinks, videoCapturer, signalingParameters)
    }

    fun createPeerConnection(
        localRender: VideoSink?,
        remoteSinks: List<VideoSink>,
        videoCapturer: VideoCapturer?,
        signalingParameters: SignalingParameters?
    ) {
        this.localRender = localRender
        this.remoteSinks = remoteSinks
        this.videoCapturer = videoCapturer
        this.signalingParameters = signalingParameters
        executor.execute {
            try {
                createMediaConstraintsInternal()
                createPeerConnectionInternal()
                maybeCreateAndStartRtcEventLog()
            } catch (e: Exception) {
                reportError("Failed to create peer connection: " + e.message)
                throw e
            }
        }
    }

    fun close() {
        executor.execute { closeInternal() }
    }

    private fun createPeerConnectionFactoryInternal(options: Options?) {
        isError = false
        if (peerConnectionParameters.tracing) {
            WebRtcPeerConnectionFactory.startInternalTracingCapture(
                "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}webrtc-trace.txt"
            )
        }

        // Check if ISAC is used by default.
        preferIsac = (peerConnectionParameters.audioCodec != null
            && peerConnectionParameters.audioCodec == AUDIO_CODEC_ISAC)

        // It is possible to save a copy in raw PCM format on a file by checking
        // the "Save input audio to file" checkbox in the Settings UI. A callback
        // interface is set when this flag is enabled. As a result, a copy of recorded
        // audio samples are provided to this client directly from the native audio
        // layer in Java.
        if (peerConnectionParameters.saveInputAudioToFile) {
            if (!peerConnectionParameters.useOpenSLES) {
                Log.d(TAG, "Enable recording of microphone input audio to file")
                saveRecordedAudioToFile = RecordedAudioToFileController(executor)
            } else {
                // TODO(henrika): ensure that the UI reflects that if OpenSL ES is selected,
                // then the "Save inut audio to file" option shall be grayed out.
                Log.e(TAG, "Recording of input audio is not supported for OpenSL ES")
            }
        }
        val adm = createJavaAudioDevice()

        // Create peer connection factory.
        if (options != null) {
            Log.d(TAG, "Factory networkIgnoreMask option: " + options.networkIgnoreMask)
        }
        //    final boolean enableH264HighProfile =
//        VIDEO_CODEC_H264_HIGH.equals(peerConnectionParameters.videoCodec);
//    final VideoEncoderFactory encoderFactory;
//    final VideoDecoderFactory decoderFactory;
//
//    if (peerConnectionParameters.videoCodecHwAcceleration) {
//      encoderFactory = new DefaultVideoEncoderFactory(
//          rootEglBase.getEglBaseContext(), true /* enableIntelVp8Encoder */, enableH264HighProfile);
//      decoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());
//    } else {
//      encoderFactory = new SoftwareVideoEncoderFactory();
//      decoderFactory = new SoftwareVideoDecoderFactory();
//    }
        factory = PeerConnectionFactory.build(options, rootEglBase, adm)
        Log.d(TAG, "Peer connection factory created.")
        adm.release()
    }

    fun createJavaAudioDevice(): AudioDeviceModule {
        // Enable/disable OpenSL ES playback.
        if (!peerConnectionParameters.useOpenSLES) {
            Log.w(TAG, "External OpenSLES ADM not implemented yet.")
            // TODO(magjed): Add support for external OpenSLES ADM.
        }

        // Set audio record error callbacks.
        val audioRecordErrorCallback: AudioRecordErrorCallback = object : AudioRecordErrorCallback {
            override fun onWebRtcAudioRecordInitError(errorMessage: String) {
                Log.e(TAG, "onWebRtcAudioRecordInitError: $errorMessage")
                reportError(errorMessage)
            }

            override fun onWebRtcAudioRecordStartError(
                errorCode: JavaAudioDeviceModule.AudioRecordStartErrorCode, errorMessage: String
            ) {
                Log.e(TAG, "onWebRtcAudioRecordStartError: $errorCode. $errorMessage")
                reportError(errorMessage)
            }

            override fun onWebRtcAudioRecordError(errorMessage: String) {
                Log.e(TAG, "onWebRtcAudioRecordError: $errorMessage")
                reportError(errorMessage)
            }
        }
        val audioTrackErrorCallback: AudioTrackErrorCallback = object : AudioTrackErrorCallback {
            override fun onWebRtcAudioTrackInitError(errorMessage: String) {
                Log.e(TAG, "onWebRtcAudioTrackInitError: $errorMessage")
                reportError(errorMessage)
            }

            override fun onWebRtcAudioTrackStartError(
                errorCode: JavaAudioDeviceModule.AudioTrackStartErrorCode, errorMessage: String
            ) {
                Log.e(TAG, "onWebRtcAudioTrackStartError: $errorCode. $errorMessage")
                reportError(errorMessage)
            }

            override fun onWebRtcAudioTrackError(errorMessage: String) {
                Log.e(TAG, "onWebRtcAudioTrackError: $errorMessage")
                reportError(errorMessage)
            }
        }

        // Set audio record state callbacks.
        val audioRecordStateCallback: AudioRecordStateCallback = object : AudioRecordStateCallback {
            override fun onWebRtcAudioRecordStart() {
                Log.i(TAG, "Audio recording starts")
            }

            override fun onWebRtcAudioRecordStop() {
                Log.i(TAG, "Audio recording stops")
            }
        }

        // Set audio track state callbacks.
        val audioTrackStateCallback: AudioTrackStateCallback = object : AudioTrackStateCallback {
            override fun onWebRtcAudioTrackStart() {
                Log.i(TAG, "Audio playout starts")
            }

            override fun onWebRtcAudioTrackStop() {
                Log.i(TAG, "Audio playout stops")
            }
        }
        return JavaAudioDeviceModule.builder(appContext)
            .setSamplesReadyCallback(saveRecordedAudioToFile)
            .setUseHardwareAcousticEchoCanceler(!peerConnectionParameters.disableBuiltInAEC)
            .setUseHardwareNoiseSuppressor(!peerConnectionParameters.disableBuiltInNS)
            .setAudioRecordErrorCallback(audioRecordErrorCallback)
            .setAudioTrackErrorCallback(audioTrackErrorCallback)
            .setAudioRecordStateCallback(audioRecordStateCallback)
            .setAudioTrackStateCallback(audioTrackStateCallback)
            .createAudioDeviceModule()
    }

    private fun createMediaConstraintsInternal() {
        // Create video constraints if video call is enabled.
        if (isVideoCallEnabled) {
            videoWidth = peerConnectionParameters.videoWidth
            videoHeight = peerConnectionParameters.videoHeight
            videoFps = peerConnectionParameters.videoFps

            // If video resolution is not specified, default to HD.
            if (videoWidth == 0 || videoHeight == 0) {
                videoWidth = HD_VIDEO_WIDTH
                videoHeight = HD_VIDEO_HEIGHT
            }

            // If fps is not specified, default to 30.
            if (videoFps == 0) {
                videoFps = 30
            }
            Logging.d(TAG, "Capturing format: " + videoWidth + "x" + videoHeight + "@" + videoFps)
        }

        // Create audio constraints.
        audioConstraints = MediaConstraints()
        // added for audio performance measurements
        if (peerConnectionParameters.noAudioProcessing) {
            Log.d(TAG, "Disabling audio processing")
            audioConstraints!!.mandatory.add(
                MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "false")
            )
            audioConstraints!!.mandatory.add(
                MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false")
            )
            audioConstraints!!.mandatory.add(
                MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false")
            )
            audioConstraints!!.mandatory.add(
                MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "false")
            )
        }
        // Create SDP constraints.
        sdpMediaConstraints = MediaConstraints()
        sdpMediaConstraints!!.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true")
        )
        sdpMediaConstraints!!.mandatory.add(
            MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", java.lang.Boolean.toString(isVideoCallEnabled)
            )
        )
    }

    private fun createPeerConnectionInternal() {
        if (factory == null || isError) {
            Log.e(TAG, "Peerconnection factory is not created")
            return
        }
        Log.d(TAG, "Create peer connection.")
        queuedRemoteCandidates = mutableListOf()
        val iceServers = signalingParameters!!.iceServers.map {
            IceServer(it.urls, it.username, it.password)
        }
        val rtcConfiguration = RtcConfiguration(
            iceServers = iceServers,
            tcpCandidatePolicy = TcpCandidatePolicy.Disabled,
            rtcpMuxPolicy = RtcpMuxPolicy.Require,
            continualGatheringPolicy = ContinualGatheringPolicy.GatherContinually,
            keyType = KeyType.ECDSA,
            enableDtlsSrtp = !peerConnectionParameters.loopback,
            sdpSemantics = SdpSemantics.UnifiedPlan
        )
        peerConnection = factory!!.createPeerConnection(rtcConfiguration, pcObserver)
        if (dataChannelEnabled) {
            val dcParams = peerConnectionParameters.dataChannelParameters!!
            dataChannel = peerConnection!!.createDataChannel(
                "ApprtcDemo data",
                id = dcParams.id,
                ordered = dcParams.ordered,
                maxRetransmitTimeMs = dcParams.maxRetransmitTimeMs,
                maxRetransmits = dcParams.maxRetransmits,
                protocol = dcParams.protocol,
                negotiated = dcParams.negotiated
            )
        }
        isInitiator = false

        // Set INFO libjingle logging.
        // NOTE: this _must_ happen while |factory| is alive!
        Logging.enableLogToDebugOutput(Logging.Severity.LS_INFO)
        val mediaStreamLabels = listOf("ARDAMS")
        if (isVideoCallEnabled) {
            peerConnection!!.native.addTrack(createVideoTrack(videoCapturer), mediaStreamLabels)
            // We can add the renderers right away because we don't need to wait for an
            // answer to get the remote track.
            remoteVideoTrack = getRemoteVideoTrack()
            remoteVideoTrack!!.setEnabled(renderVideo)
            for (remoteSink in remoteSinks!!) {
                remoteVideoTrack!!.addSink(remoteSink)
            }
        }
        peerConnection!!.native.addTrack(createAudioTrack(), mediaStreamLabels)
        if (isVideoCallEnabled) {
            findVideoSender()
        }
        if (peerConnectionParameters.aecDump) {
            try {
                val aecDumpFileDescriptor = ParcelFileDescriptor.open(
                    File("${Environment.getExternalStorageDirectory().path}${File.separator}Download/audio.aecdump"),
                    ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE
                        or ParcelFileDescriptor.MODE_TRUNCATE
                )
                factory!!.startAecDump(aecDumpFileDescriptor.detachFd(), -1)
            } catch (e: IOException) {
                Log.e(TAG, "Can not open aecdump file", e)
            }
        }
        if (saveRecordedAudioToFile != null) {
            if (saveRecordedAudioToFile!!.start()) {
                Log.d(TAG, "Recording input audio to file is activated")
            }
        }
        Log.d(TAG, "Peer connection created.")
    }

    private fun createRtcEventLogOutputFile(): File {
        val dateFormat: DateFormat = SimpleDateFormat("yyyyMMdd_hhmm_ss", Locale.getDefault())
        val date = Date()
        val outputFileName = "event_log_" + dateFormat.format(date) + ".log"
        return File(
            appContext!!.getDir(RTCEVENTLOG_OUTPUT_DIR_NAME, Context.MODE_PRIVATE), outputFileName
        )
    }

    private fun maybeCreateAndStartRtcEventLog() {
        if (appContext == null || peerConnection == null) {
            return
        }
        if (!peerConnectionParameters.enableRtcEventLog) {
            Log.d(TAG, "RtcEventLog is disabled.")
            return
        }
        rtcEventLog = RtcEventLog(peerConnection!!.native)
        rtcEventLog!!.start(createRtcEventLogOutputFile())
    }

    private fun closeInternal() {
        if (factory != null && peerConnectionParameters.aecDump) {
            factory!!.stopAecDump()
        }
        Log.d(TAG, "Closing peer connection.")
        statsTimer.cancel()
        if (dataChannel != null) {
            dataChannel!!.dispose()
            dataChannel = null
        }
        if (rtcEventLog != null) {
            // RtcEventLog should stop before the peer connection is disposed.
            rtcEventLog!!.stop()
            rtcEventLog = null
        }
        if (peerConnection != null) {
            peerConnection!!.native.dispose()
            peerConnection = null
        }
        Log.d(TAG, "Closing audio source.")
        if (audioSource != null) {
            audioSource!!.dispose()
            audioSource = null
        }
        Log.d(TAG, "Stopping capture.")
        if (videoCapturer != null) {
            try {
                videoCapturer!!.stopCapture()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
            videoCapturerStopped = true
            videoCapturer!!.dispose()
            videoCapturer = null
        }
        Log.d(TAG, "Closing video source.")
        if (videoSource != null) {
            videoSource!!.dispose()
            videoSource = null
        }
        if (surfaceTextureHelper != null) {
            surfaceTextureHelper!!.dispose()
            surfaceTextureHelper = null
        }
        if (saveRecordedAudioToFile != null) {
            Log.d(TAG, "Closing audio file for recorded input audio.")
            saveRecordedAudioToFile!!.stop()
            saveRecordedAudioToFile = null
        }
        localRender = null
        remoteSinks = null
        Log.d(TAG, "Closing peer connection factory.")
        if (factory != null) {
            factory!!.dispose()
            factory = null
        }
        rootEglBase.release()
        Log.d(TAG, "Closing peer connection done.")
        events.onPeerConnectionClosed()
        WebRtcPeerConnectionFactory.stopInternalTracingCapture()
        WebRtcPeerConnectionFactory.shutdownInternalTracer()
    }

    fun enableStatsEvents(enable: Boolean, periodMs: Int) {
        if (enable) {
            try {
                statsTimer.schedule(object : TimerTask() {
                    override fun run() {
                        executor.execute { stats }
                    }
                }, 0, periodMs.toLong())
            } catch (e: Exception) {
                Log.e(TAG, "Can not schedule statistics timer", e)
            }
        } else {
            statsTimer.cancel()
        }
    }

    fun setAudioEnabled(enable: Boolean) {
        executor.execute {
            enableAudio = enable
            if (localAudioTrack != null) {
                localAudioTrack!!.setEnabled(enableAudio)
            }
        }
    }

    fun setVideoEnabled(enable: Boolean) {
        executor.execute {
            renderVideo = enable
            if (localVideoTrack != null) {
                localVideoTrack!!.setEnabled(renderVideo)
            }
            if (remoteVideoTrack != null) {
                remoteVideoTrack!!.setEnabled(renderVideo)
            }
        }
    }

    fun createOffer() {
        executor.execute {
            if (peerConnection != null && !isError) {
                Log.d(TAG, "PC Create OFFER")
                isInitiator = true
                peerConnection!!.native.createOffer(sdpObserver, sdpMediaConstraints)
            }
        }
    }

    fun createAnswer() {
        executor.execute {
            if (peerConnection != null && !isError) {
                Log.d(TAG, "PC create ANSWER")
                isInitiator = false
                peerConnection!!.native.createAnswer(sdpObserver, sdpMediaConstraints)
            }
        }
    }

    fun addRemoteIceCandidate(candidate: IceCandidate) {
        executor.execute {
            if (peerConnection != null && !isError) {
                if (queuedRemoteCandidates != null) {
                    queuedRemoteCandidates!!.add(candidate)
                } else {
                    peerConnection!!.addIceCandidate(candidate)
                }
            }
        }
    }

    fun removeRemoteIceCandidates(candidates: List<IceCandidate>) {
        executor.execute {
            if (peerConnection == null || isError) {
                return@execute
            }
            // Drain the queued remote candidates if there is any so that
            // they are processed in the proper order.
            drainCandidates()
            peerConnection!!.removeIceCandidates(candidates)
        }
    }

    fun setRemoteDescription(desc: SessionDescription) {
        executor.execute {
            if (peerConnection == null || isError) {
                return@execute
            }
            var sdp = desc.description
            if (preferIsac) {
                sdp = preferCodec(sdp, AUDIO_CODEC_ISAC, true)
            }
            if (isVideoCallEnabled) {
                sdp = preferCodec(sdp, getSdpVideoCodecName(peerConnectionParameters), false)
            }
            if (peerConnectionParameters.audioStartBitrate > 0) {
                sdp = setStartBitrate(
                    AUDIO_CODEC_OPUS, false, sdp, peerConnectionParameters.audioStartBitrate
                )
            }
            Log.d(TAG, "Set remote SDP.")
            val sdpRemote = SessionDescription(desc.type, sdp)
            peerConnection!!.native.setRemoteDescription(sdpObserver, sdpRemote)
        }
    }

    fun stopVideoSource() {
        executor.execute {
            if (videoCapturer != null && !videoCapturerStopped) {
                Log.d(TAG, "Stop video source.")
                try {
                    videoCapturer!!.stopCapture()
                } catch (e: InterruptedException) {
                }
                videoCapturerStopped = true
            }
        }
    }

    fun startVideoSource() {
        executor.execute {
            if (videoCapturer != null && videoCapturerStopped) {
                Log.d(TAG, "Restart video source.")
                videoCapturer!!.startCapture(videoWidth, videoHeight, videoFps)
                videoCapturerStopped = false
            }
        }
    }

    fun setVideoMaxBitrate(maxBitrateKbps: Int?) {
        executor.execute {
            if (peerConnection == null || localVideoSender == null || isError) {
                return@execute
            }
            Log.d(TAG, "Requested max video bitrate: $maxBitrateKbps")
            if (localVideoSender == null) {
                Log.w(TAG, "Sender is not ready.")
                return@execute
            }
            val parameters = localVideoSender!!.parameters
            if (parameters.encodings.size == 0) {
                Log.w(TAG, "RtpParameters are not ready.")
                return@execute
            }
            for (encoding in parameters.encodings) {
                // Null value means no limit.
                encoding.maxBitrateBps =
                    if (maxBitrateKbps == null) null else maxBitrateKbps * BPS_IN_KBPS
            }
            if (!localVideoSender!!.setParameters(parameters)) {
                Log.e(TAG, "RtpSender.setParameters failed.")
            }
            Log.d(TAG, "Configured max video bitrate to: $maxBitrateKbps")
        }
    }

    private fun reportError(errorMessage: String) {
        Log.e(TAG, "Peerconnection error: $errorMessage")
        executor.execute {
            if (!isError) {
                events.onPeerConnectionError(errorMessage)
                isError = true
            }
        }
    }

    private fun createAudioTrack(): AudioTrack? {
        audioSource = factory!!.native.createAudioSource(audioConstraints)
        localAudioTrack = factory!!.native.createAudioTrack(AUDIO_TRACK_ID, audioSource)
        localAudioTrack!!.setEnabled(enableAudio)
        return localAudioTrack
    }

    private fun createVideoTrack(capturer: VideoCapturer?): VideoTrack? {
        surfaceTextureHelper =
            SurfaceTextureHelper.create("CaptureThread", rootEglBase.eglBaseContext)
        videoSource = factory!!.native.createVideoSource(capturer!!.isScreencast)
        capturer.initialize(surfaceTextureHelper, appContext, videoSource!!.capturerObserver)
        capturer.startCapture(videoWidth, videoHeight, videoFps)
        localVideoTrack = factory!!.native.createVideoTrack(VIDEO_TRACK_ID, videoSource)
        localVideoTrack!!.setEnabled(renderVideo)
        localVideoTrack!!.addSink(localRender)
        return localVideoTrack
    }

    private fun findVideoSender() {
        for (sender in peerConnection!!.native.senders) {
            if (sender.track() != null) {
                val trackType = sender.track()!!.kind()
                if (trackType == VIDEO_TRACK_TYPE) {
                    Log.d(TAG, "Found video sender.")
                    localVideoSender = sender
                }
            }
        }
    }

    // Returns the remote VideoTrack, assuming there is only one.
    private fun getRemoteVideoTrack(): VideoTrack? {
        for (transceiver in peerConnection!!.native.transceivers) {
            val track = transceiver.receiver.track()
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

    private fun switchCameraInternal() {
        if (videoCapturer is CameraVideoCapturer) {
            if (!isVideoCallEnabled || isError) {
                Log.e(
                    TAG,
                    "Failed to switch camera. Video: $isVideoCallEnabled. Error : $isError"
                )
                return  // No video is sent or only one camera is available or error happened.
            }
            Log.d(TAG, "Switch camera")
            val cameraVideoCapturer = videoCapturer as CameraVideoCapturer
            cameraVideoCapturer.switchCamera(null)
        } else {
            Log.d(TAG, "Will not switch camera, video caputurer is not a camera")
        }
    }

    fun switchCamera() {
        executor.execute { switchCameraInternal() }
    }

    fun changeCaptureFormat(width: Int, height: Int, framerate: Int) {
        executor.execute { changeCaptureFormatInternal(width, height, framerate) }
    }

    private fun changeCaptureFormatInternal(width: Int, height: Int, framerate: Int) {
        if (!isVideoCallEnabled || isError || videoCapturer == null) {
            Log.e(
                TAG,
                "Failed to change capture format. Video: " + isVideoCallEnabled
                    + ". Error : " + isError
            )
            return
        }
        Log.d(TAG, "changeCaptureFormat: " + width + "x" + height + "@" + framerate)
        videoSource!!.adaptOutputFormat(width, height, framerate)
    }

    // Implementation detail: observe ICE & stream changes and react accordingly.
    private inner class PCObserver : PeerConnectionObserver {
        override fun onIceConnectionReceivingChange(receiving: Boolean) {
            Log.d(TAG, "IceConnectionReceiving changed to $receiving")
        }

        override fun onRenegotiationNeeded() {
            // No need to do anything; AppRTC follows a pre-agreed-upon
            // signaling/negotiation protocol.
        }

        override fun onSignalingChange(newState: SignalingState) {
            Log.d(TAG, "SignalingState: $newState")
        }

        override fun onIceConnectionChange(newState: IceConnectionState) {
            executor.execute {
                Log.d(TAG, "IceConnectionState: $newState")
                if (newState === IceConnectionState.Connected) {
                    events.onIceConnected()
                } else if (newState === IceConnectionState.Disconnected) {
                    events.onIceDisconnected()
                } else if (newState === IceConnectionState.Failed) {
                    reportError("ICE connection failed.")
                }
            }
        }

        override fun onConnectionChange(newState: PeerConnectionState) {
            executor.execute {
                Log.d(TAG, "PeerConnectionState: $newState")
                if (newState === PeerConnectionState.Connected) {
                    events.onConnected()
                } else if (newState === PeerConnectionState.Disconnected) {
                    events.onDisconnected()
                } else if (newState === PeerConnectionState.Failed) {
                    reportError("DTLS connection failed.")
                }
            }
        }

        override fun onIceGatheringChange(newState: IceGatheringState) {
            Log.d(TAG, "IceGatheringState: $newState")
        }

        override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent) {
            Log.d(TAG, "Selected candidate pair changed because: $event")
        }

        override fun onIceCandidate(candidate: IceCandidate) {
            executor.execute { events.onIceCandidate(candidate) }
        }

        override fun onIceCandidatesRemoved(candidates: List<IceCandidate>) {
            executor.execute { events.onIceCandidatesRemoved(candidates) }
        }

        override fun onAddStream(stream: MediaStream) {}
        override fun onRemoveStream(stream: MediaStream) {}

        override fun onDataChannel(dataChannel: DataChannel) {
            Log.d(TAG, "New Data channel " + dataChannel.label)
            if (!dataChannelEnabled) return
            dataChannel.registerObserver(object : DataChannelObserver {
                override fun onMessage(buffer: DataChannelBuffer) {
                    if (buffer.binary) {
                        Log.d(TAG, "Received binary msg over $dataChannel")
                        return
                    }
                    val bytes = buffer.data
                    val strData = String(bytes, Charset.forName("UTF-8"))
                    Log.d(TAG, "Got msg: $strData over $dataChannel")
                }

                override fun onBufferedAmountChange(previousAmount: Long) {
                    Log.d(
                        TAG,
                        "Data channel buffered amount changed: " + dataChannel.label + ": " + dataChannel.state
                    )
                }

                override fun onStateChange() {
                    Log.d(
                        TAG,
                        "Data channel state changed: " + dataChannel.label + ": " + dataChannel.state
                    )
                }
            })
        }

        override fun onAddTrack(receiver: RtpReceiver, streams: List<MediaStream>) {}
        override fun onTrack(transceiver: RtpTransceiver) {}
    }

    // Implementation detail: handle offer creation/signaling and answer setting,
    // as well as adding remote ICE candidates once the answer SDP is set.
    private inner class SDPObserver : SdpObserver {
        override fun onCreateSuccess(desc: SessionDescription) {
            if (localDescription != null) {
                reportError("Multiple SDP create.")
                return
            }
            var sdp = desc.description
            if (preferIsac) {
                sdp = preferCodec(sdp, AUDIO_CODEC_ISAC, true)
            }
            if (isVideoCallEnabled) {
                sdp = preferCodec(sdp, getSdpVideoCodecName(peerConnectionParameters), false)
            }
            val newDesc = SessionDescription(desc.type, sdp)
            localDescription = newDesc
            executor.execute {
                if (peerConnection != null && !isError) {
                    Log.d(TAG, "Set local SDP from " + desc.type)
                    peerConnection!!.native.setLocalDescription(sdpObserver, newDesc)
                }
            }
        }

        override fun onSetSuccess() {
            executor.execute {
                if (peerConnection == null || isError) {
                    return@execute
                }
                if (isInitiator) {
                    // For offering peer connection we first create offer and set
                    // local SDP, then after receiving answer set remote SDP.
                    if (peerConnection!!.native.remoteDescription == null) {
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
                    if (peerConnection!!.native.localDescription != null) {
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

        override fun onCreateFailure(error: String) {
            reportError("createSDP error: $error")
        }

        override fun onSetFailure(error: String) {
            reportError("setSDP error: $error")
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
        @JvmField val videoCallEnabled: Boolean,
        val loopback: Boolean,
        val tracing: Boolean,
        val videoWidth: Int,
        val videoHeight: Int,
        val videoFps: Int,
        @JvmField val videoMaxBitrate: Int,
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

        // Executor thread is started once in private ctor and is used for all
        // peer connection API calls to ensure new peer connection factory is
        // created on the same thread as previously destroyed factory.
        private val executor = Executors.newSingleThreadExecutor()
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

        private fun setStartBitrate(
            codec: String,
            isVideoCodec: Boolean,
            sdp: String,
            bitrateKbps: Int
        ): String {
            val lines = sdp.split("\r\n").toTypedArray()
            var rtpmapLineIndex = -1
            var sdpFormatUpdated = false
            var codecRtpMap: String? = null
            // Search for codec rtpmap in format
            // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
            var regex = "^a=rtpmap:(\\d+) $codec(/\\d+)+[\r]?$"
            var codecPattern = Pattern.compile(regex)
            for (i in lines.indices) {
                val codecMatcher = codecPattern.matcher(lines[i])
                if (codecMatcher.matches()) {
                    codecRtpMap = codecMatcher.group(1)
                    rtpmapLineIndex = i
                    break
                }
            }
            if (codecRtpMap == null) {
                Log.w(TAG, "No rtpmap for $codec codec")
                return sdp
            }
            Log.d(
                TAG,
                "Found " + codec + " rtpmap " + codecRtpMap + " at " + lines[rtpmapLineIndex]
            )

            // Check if a=fmtp string already exist in remote SDP for this codec and
            // update it with new bitrate parameter.
            regex = "^a=fmtp:$codecRtpMap \\w+=\\d+.*[\r]?$"
            codecPattern = Pattern.compile(regex)
            for (i in lines.indices) {
                val codecMatcher = codecPattern.matcher(lines[i])
                if (codecMatcher.matches()) {
                    Log.d(TAG, "Found " + codec + " " + lines[i])
                    if (isVideoCodec) {
                        lines[i] += "; " + VIDEO_CODEC_PARAM_START_BITRATE + "=" + bitrateKbps
                    } else {
                        lines[i] += "; " + AUDIO_CODEC_PARAM_BITRATE + "=" + bitrateKbps * 1000
                    }
                    Log.d(TAG, "Update remote SDP line: " + lines[i])
                    sdpFormatUpdated = true
                    break
                }
            }
            val newSdpDescription = StringBuilder()
            for (i in lines.indices) {
                newSdpDescription.append(lines[i]).append("\r\n")
                // Append new a=fmtp line if no such line exist for a codec.
                if (!sdpFormatUpdated && i == rtpmapLineIndex) {
                    var bitrateSet: String
                    bitrateSet = if (isVideoCodec) {
                        "a=fmtp:$codecRtpMap $VIDEO_CODEC_PARAM_START_BITRATE=$bitrateKbps"
                    } else {
                        "a=fmtp:$codecRtpMap $AUDIO_CODEC_PARAM_BITRATE=${bitrateKbps * 1000}"
                    }
                    Log.d(TAG, "Add remote SDP line: $bitrateSet")
                    newSdpDescription.append(bitrateSet).append("\r\n")
                }
            }
            return newSdpDescription.toString()
        }

        /** Returns the line number containing "m=audio|video", or -1 if no such line exists.  */
        private fun findMediaDescriptionLine(isAudio: Boolean, sdpLines: List<String>): Int {
            val mediaDescription = if (isAudio) "m=audio " else "m=video "
            return sdpLines.indexOfFirst { it.startsWith(mediaDescription) }
        }

        private fun movePayloadTypesToFront(
            preferredPayloadTypes: List<String>,
            mLine: String
        ): String? {
            // The format of the media description line should be: m=<media> <port> <proto> <fmt> ...
            val origLineParts = mLine.split(" ")
            if (origLineParts.size <= 3) {
                Log.e(TAG, "Wrong SDP media description format: $mLine")
                return null
            }
            val header: List<String> = origLineParts.subList(0, 3)
            val unpreferredPayloadTypes =
                mutableListOf(*origLineParts.subList(3, origLineParts.size).toTypedArray())
            unpreferredPayloadTypes.removeAll(preferredPayloadTypes)
            // Reconstruct the line with |preferredPayloadTypes| moved to the beginning of the payload
            // types.
            val newLineParts = mutableListOf<String>()
            newLineParts.addAll(header)
            newLineParts.addAll(preferredPayloadTypes)
            newLineParts.addAll(unpreferredPayloadTypes)
            return newLineParts.joinToString(separator = " ")
        }

        private fun preferCodec(sdp: String, codec: String, isAudio: Boolean): String {
            val lines = sdp.trim().split("\r\n").toMutableList()
            val mLineIndex = findMediaDescriptionLine(isAudio, lines)
            if (mLineIndex == -1) {
                Log.w(TAG, "No mediaDescription line, so can't prefer $codec")
                return sdp
            }
            // A list with all the payload types with name |codec|. The payload types are integers in the
            // range 96-127, but they are stored as strings here.
            val codecPayloadTypes = mutableListOf<String>()
            // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
            val codecPattern = Pattern.compile("^a=rtpmap:(\\d+) $codec(/\\d+)+[\r]?$")
            for (line in lines) {
                val codecMatcher = codecPattern.matcher(line)
                if (codecMatcher.matches()) {
                    codecPayloadTypes.add(codecMatcher.group(1)!!)
                }
            }
            if (codecPayloadTypes.isEmpty()) {
                Log.w(TAG, "No payload types with name $codec")
                return sdp
            }
            val newMLine = movePayloadTypesToFront(codecPayloadTypes, lines[mLineIndex])
                ?: return sdp

            Log.d(TAG, "Change media description from: ${lines[mLineIndex]} to $newMLine")
            lines[mLineIndex] = newMLine
            return lines.joinToString(separator = "\r\n", postfix = "\r\n")
        }
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
        fun onPeerConnectionStatsReady(reports: Array<StatsReport>)

        /**
         * Callback fired once peer connection error happened.
         */
        fun onPeerConnectionError(description: String?)
    }
}
