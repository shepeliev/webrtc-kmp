@file:JvmName("AndroidPeerConnection")
@file:Suppress("unused")

package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlin.jvm.JvmName

expect class PeerConnection(rtcConfiguration: RtcConfiguration = RtcConfiguration()) {
    val localDescription: SessionDescription?
    val remoteDescription: SessionDescription?
    val signalingState: SignalingState
    val iceConnectionState: IceConnectionState
    val connectionState: PeerConnectionState
    val iceGatheringState: IceGatheringState

    internal val peerConnectionEvent: Flow<PeerConnectionEvent>

    fun createDataChannel(
        label: String,
        id: Int = -1,
        ordered: Boolean = true,
        maxRetransmitTimeMs: Int = -1,
        maxRetransmits: Int = -1,
        protocol: String = "",
        negotiated: Boolean = false,
    ): DataChannel?

    suspend fun createOffer(options: OfferAnswerOptions): SessionDescription
    suspend fun createAnswer(options: OfferAnswerOptions): SessionDescription
    suspend fun setLocalDescription(description: SessionDescription)
    suspend fun setRemoteDescription(description: SessionDescription)

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
    fun addTrack(track: MediaStreamTrack, vararg streams: MediaStream): RtpSender

    /**
     * Stops sending media from sender. The sender will still appear in getSenders. Future
     * calls to createOffer will mark the m section for the corresponding transceiver as
     * receive only or inactive, as defined in JSEP. Returns true on success.
     */
    fun removeTrack(sender: RtpSender): Boolean

    /**
     * Gets stats using the new stats collection API, see webrtc/api/stats/.
     */
    suspend fun getStats(): RtcStatsReport?
    suspend fun getStats(sender: RtpSender): RtcStatsReport?
    suspend fun getStats(receiver: RtpReceiver): RtcStatsReport?

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
    get() = peerConnectionEvent
        .map { it as? PeerConnectionEvent.ConnectionStateChange }
        .filterNotNull()
        .map { it.state }

/**
 * Emits [DataChannel] events. This event is sent when an [DataChannel] is added to the connection
 * by the remote peer calling [PeerConnection.createDataChannel]
 */
val PeerConnection.onDataChannel: Flow<DataChannel>
    get() = peerConnectionEvent
        .map { it as? PeerConnectionEvent.NewDataChannel }
        .filterNotNull()
        .map { it.dataChannel }

/**
 * Emits [IceCandidate] events. This happens whenever the local ICE agent needs to deliver a message
 * to the other peer through the signaling server.
 */
val PeerConnection.onIceCandidate: Flow<IceCandidate>
    get() = peerConnectionEvent
        .map { it as? PeerConnectionEvent.NewIceCandidate }
        .filterNotNull()
        .map { it.candidate }

/**
 * Emits [IceConnectionState] events. This happens when the state of the connection's ICE agent,
 * as represented by the [PeerConnection.iceConnectionState] property, changes.
 */
val PeerConnection.onIceConnectionStateChange: Flow<IceConnectionState>
    get() = peerConnectionEvent
        .map { it as? PeerConnectionEvent.IceConnectionStateChange }
        .filterNotNull()
        .map { it.state }

/**
 * Emits [IceGatheringState] events. This happens when the ICE gathering state—that is, whether or
 * not the ICE agent is actively gathering candidates—changes.
 */
val PeerConnection.onIceGatheringState: Flow<IceGatheringState>
    get() = peerConnectionEvent
        .map { it as? PeerConnectionEvent.IceGatheringStateChange }
        .filterNotNull()
        .map { it.state }

/**
 * Emits negotiationneeded events. This event is fired when a change has occurred which requires
 * session negotiation. This negotiation should be carried out as the offerer, because some session
 * changes cannot be negotiated as the answerer.
 */
val PeerConnection.onNegotiationNeeded: Flow<Unit>
    get() = peerConnectionEvent
        .filter { it is PeerConnectionEvent.NegotiationNeeded }
        .map { }

/**
 * Emits [SignalingState] events..
 */
val PeerConnection.onSignalingStateChange: Flow<SignalingState>
    get() = peerConnectionEvent
        .map { it as? PeerConnectionEvent.SignalingStateChange }
        .filterNotNull()
        .map { it.state }

/**
 * Emits track events, indicating that a track has been added to the [PeerConnection].
 */
val PeerConnection.onTrack: Flow<TrackEvent>
    get() = peerConnectionEvent
        .map { it as? PeerConnectionEvent.Track }
        .filterNotNull()
        .map { it.trackEvent }

val PeerConnection.onStandardizedIceConnection: Flow<IceConnectionState>
    get() = peerConnectionEvent
        .map { it as? PeerConnectionEvent.StandardizedIceConnectionChange }
        .filterNotNull()
        .map { it.state }

val PeerConnection.onRemovedIceCandidates: Flow<List<IceCandidate>>
    get() = peerConnectionEvent
        .map { it as? PeerConnectionEvent.RemovedIceCandidates }
        .filterNotNull()
        .map { it.candidates }

val PeerConnection.onRemoveTrack: Flow<RtpReceiver>
    get() = peerConnectionEvent
        .map { it as? PeerConnectionEvent.RemoveTrack }
        .filterNotNull()
        .map { it.rtpReceiver }

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

enum class ContinualGatheringPolicy { GatherOnce, GatherContinually }
