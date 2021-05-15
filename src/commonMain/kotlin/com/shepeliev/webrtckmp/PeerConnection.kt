@file:JvmName("AndroidPeerConnection")

package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.jvm.JvmName

expect class PeerConnection(rtcConfiguration: RtcConfiguration = RtcConfiguration()) {
    val localDescription: SessionDescription?
    val remoteDescription: SessionDescription?
    val certificate: RtcCertificatePem?
    val signalingState: SignalingState
    val iceConnectionState: IceConnectionState
    val connectionState: PeerConnectionState
    val iceGatheringState: IceGatheringState
    internal val events: PeerConnectionEvents

    fun createDataChannel(
        label: String,
        id: Int = -1,
        ordered: Boolean = true,
        maxRetransmitTimeMs: Int = -1,
        maxRetransmits: Int = -1,
        protocol: String = "",
        negotiated: Boolean = false,
    ): DataChannel?

    suspend fun createOffer(constraints: MediaConstraints): SessionDescription
    suspend fun createAnswer(constraints: MediaConstraints): SessionDescription
    suspend fun setLocalDescription(description: SessionDescription)
    suspend fun setRemoteDescription(description: SessionDescription)

    /**
     * Enables/disables playout of received audio streams. Enabled by default.
     *
     * Note that even if playout is enabled, streams will only be played out if
     * the appropriate SDP is also applied. The main purpose of this API is to
     * be able to control the exact time when audio playout starts.
     */
    fun setAudioPlayout(playout: Boolean)

    /**
     * Enables/disables recording of transmitted audio streams. Enabled by default.
     *
     * Note that even if recording is enabled, streams will only be recorded if
     * the appropriate SDP is also applied. The main purpose of this API is to
     * be able to control the exact time when audio recording starts.
     */
    fun setAudioRecording(recording: Boolean)

    fun setConfiguration(configuration: RtcConfiguration): Boolean
    fun addIceCandidate(candidate: IceCandidate): Boolean
    fun removeIceCandidates(candidates: List<IceCandidate>): Boolean

    /**
     * Gets all RtpSenders associated with this peer connection.
     * Note that calling getSenders will dispose of the senders previously
     * returned.
     */
    fun getSenders(): List<RtpSender>

    /**
     * Gets all RtpReceivers associated with this peer connection.
     * Note that calling getReceivers will dispose of the receivers previously
     * returned.
     */
    fun getReceivers(): List<RtpReceiver>

    /**
     * Gets all RtpTransceivers associated with this peer connection.
     * Note that calling getTransceivers will dispose of the transceivers previously
     * returned.
     * Note: This is only available with SdpSemantics.UNIFIED_PLAN specified.
     */
    fun getTransceivers(): List<RtpTransceiver>

    /**
     * Adds a new media stream track to be sent on this peer connection, and returns
     * the newly created RtpSender. If streamIds are specified, the RtpSender will
     * be associated with the streams specified in the streamIds list.
     *
     * @throws IllegalStateException if an error accors in C++ addTrack.
     * An error can occur if:
     * - A sender already exists for the track.
     * - The peer connection is closed.
     */
    fun addTrack(track: MediaStreamTrack, streamIds: List<String> = emptyList()): RtpSender

    /**
     * Stops sending media from sender. The sender will still appear in getSenders. Future
     * calls to createOffer will mark the m section for the corresponding transceiver as
     * receive only or inactive, as defined in JSEP. Returns true on success.
     */
    fun removeTrack(sender: RtpSender): Boolean

    /**
     * Creates a new RtpTransceiver and adds it to the set of transceivers. Adding a
     * transceiver will cause future calls to CreateOffer to add a media description
     * for the corresponding transceiver.
     *
     * The initial value of |mid| in the returned transceiver is null. Setting a
     * new session description may change it to a non-null value.
     *
     * https://w3c.github.io/webrtc-pc/#dom-rtcpeerconnection-addtransceiver
     *
     * If a MediaStreamTrack is specified then a transceiver will be added with a
     * sender set to transmit the given track. The kind
     * of the transceiver (and sender/receiver) will be derived from the kind of
     * the track.
     *
     * If MediaType is specified then a transceiver will be added based upon that type.
     * This can be either MEDIA_TYPE_AUDIO or MEDIA_TYPE_VIDEO.
     *
     * The transceiver will default to having a direction of kSendRecv and not be part
     * of any streams.
     *
     * Note: These methods are only available with SdpSemantics.UNIFIED_PLAN specified.
     * @throws IllegalStateException if an error accors in C++ addTransceiver
     */
    fun addTransceiver(
        track: MediaStreamTrack,
        direction: RtpTransceiverDirection = RtpTransceiverDirection.SendRecv,
        streamIds: List<String> = emptyList(),
        sendEncodings: List<RtpEncodingParameters> = emptyList()
    ): RtpTransceiver

    fun addTransceiver(
        mediaType: MediaStreamTrack.MediaType,
        direction: RtpTransceiverDirection = RtpTransceiverDirection.SendRecv,
        streamIds: List<String> = emptyList(),
        sendEncodings: List<RtpEncodingParameters> = emptyList()
    ): RtpTransceiver

    /**
     * Gets stats using the new stats collection API, see webrtc/api/stats/.
     */
    suspend fun getStats(): RtcStatsReport?

    /**
     * Limits the bandwidth allocated for all RTP streams sent by this
     * PeerConnection. Pass null to leave a value unchanged.
     */
    fun setBitrate(min: Int? = null, current: Int? = null, max: Int? = null): Boolean

    /**
     * Starts recording an RTC event log.
     *
     * Ownership of the file is transfered to the native code. If an RTC event
     * log is already being recorded, it will be stopped and a new one will start
     * using the provided file. Logging will continue until the stopRtcEventLog
     * function is called. The max_size_bytes argument is ignored, it is added
     * for future use.
     */
    fun startRtcEventLog(filePath: String, maxSizeBytes: Int): Boolean

    /**
     * Stops recording an RTC event log. If no RTC event log is currently being
     * recorded, this call will have no effect.
     */
    fun stopRtcEventLog()

    /**
     * Free native resources associated with this PeerConnection instance.
     *
     * This method removes a reference count from the C++ PeerConnection object,
     * which should result in it being destroyed. It also calls equivalent
     * "dispose" methods on the Java objects attached to this PeerConnection
     * (streams, senders, receivers), such that their associated C++ objects
     * will also be destroyed.
     *
     *
     * Note that this method cannot be safely called from an observer callback
     * (PeerConnection.Observer, DataChannel.Observer, etc.). If you want to, for
     * example, destroy the PeerConnection after an "ICE failed" callback, you
     * must do this asynchronously (in other words, unwind the stack first). See
     * [bug 3721](https://bugs.chromium.org/p/webrtc/issues/detail?id=3721) for more details.
     */
    fun close()
}

/**
 * Emits [PeerConnectionState] events. This happens whenever the aggregate state of the connection
 * changes.
 */
val PeerConnection.onConnectionStateChange: Flow<PeerConnectionState>
    get() = events.onConnectionStateChange.asSharedFlow()

/**
 * Emits [DataChannel] events. This event is sent when an [DataChannel] is added to the connection
 * by the remote peer calling [PeerConnection.createDataChannel]
 */
val PeerConnection.onDataChannel: Flow<DataChannel>
    get() = events.onDataChannel.asSharedFlow()

/**
 * Emits [IceCandidate] events. This happens whenever the local ICE agent needs to deliver a message
 * to the other peer through the signaling server.
 */
val PeerConnection.onIceCandidate: Flow<IceCandidate>
    get() = events.onIceCandidate.asSharedFlow()

/**
 * Emits [IceConnectionState] events. This happens when the state of the connection's ICE agent,
 * as represented by the [PeerConnection.iceConnectionState] property, changes.
 */
val PeerConnection.onIceConnectionStateChange: Flow<IceConnectionState>
    get() = events.onIceConnectionStateChange.asSharedFlow()

/**
 * Emits [IceGatheringState] events. This happens when the ICE gathering state—that is, whether or
 * not the ICE agent is actively gathering candidates—changes.
 */
val PeerConnection.onIceGatheringState: Flow<IceGatheringState>
    get() = events.onIceGatheringStateChange.asSharedFlow()

/**
 * Emits negotiationneeded events. This event is fired when a change has occurred which requires
 * session negotiation. This negotiation should be carried out as the offerer, because some session
 * changes cannot be negotiated as the answerer.
 */
val PeerConnection.onNegotiationNeeded: Flow<Unit>
    get() = events.onNegotiationNeeded.asSharedFlow()

/**
 * Emits [SignalingState] events..
 */
val PeerConnection.onSignalingStateChange: Flow<SignalingState>
    get() = events.onSignalingStateChange.asSharedFlow()

/**
 * Emits track events, indicating that a track has been added to the [PeerConnection].
 */
val PeerConnection.onTrack: Flow<TrackEvent>
    get() = events.onTrack.asSharedFlow()

val PeerConnection.onStandardizedIceConnection: Flow<IceConnectionState>
    get() = events.onStandardizedIceConnectionChange.asSharedFlow()

val PeerConnection.onRemovedIceCandidates: Flow<List<IceCandidate>>
    get() = events.onRemovedIceCandidates.asSharedFlow()

val PeerConnection.onRemoveTrack: Flow<RtpReceiver>
    get() = events.onRemoveTrack.asSharedFlow()

enum class TlsCertPolicy { TlsCertPolicySecure, TlsCertPolicyInsecureNoCheck }
enum class KeyType { RSA, ECDSA }
enum class RtcpMuxPolicy { Negotiate, Require }
enum class BundlePolicy { Balanced, MaxBundle, MaxCompat }
enum class IceTransportPolicy { None, Relay, NoHost, All }

enum class SignalingState {
    Stable,
    HaveLocalOffer,
    HaveLocalPranswer,
    HaveRemoteOffer,
    HaveRemotePranswer,
    Closed;
}

enum class IceConnectionState {
    New,
    Checking,
    Connected,
    Completed,
    Failed,
    Disconnected,
    Closed,
    Count;
}

enum class PeerConnectionState { New, Connecting, Connected, Disconnected, Failed, Closed; }

enum class IceGatheringState { New, Gathering, Complete }
