@file:JvmName("AndroidPeerConnection")

package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlin.jvm.JvmName

public expect class PeerConnection(
    rtcConfiguration: RtcConfiguration = RtcConfiguration(),
) {
    public val localDescription: SessionDescription?
    public val remoteDescription: SessionDescription?
    public val signalingState: SignalingState
    public val iceConnectionState: IceConnectionState
    public val connectionState: PeerConnectionState
    public val iceGatheringState: IceGatheringState

    internal val peerConnectionEvent: Flow<PeerConnectionEvent>

    public fun createDataChannel(
        label: String,
        id: Int = -1,
        ordered: Boolean = true,
        maxPacketLifeTimeMs: Int = -1,
        maxRetransmits: Int = -1,
        protocol: String = "",
        negotiated: Boolean = false,
    ): DataChannel?

    public suspend fun createOffer(options: OfferAnswerOptions): SessionDescription

    public suspend fun createAnswer(options: OfferAnswerOptions): SessionDescription

    public suspend fun setLocalDescription(description: SessionDescription)

    public suspend fun setRemoteDescription(description: SessionDescription)

    public fun setConfiguration(configuration: RtcConfiguration): Boolean

    public suspend fun addIceCandidate(candidate: IceCandidate): Boolean

    public fun removeIceCandidates(candidates: List<IceCandidate>): Boolean

    /**
     * Gets all RtpSenders associated with this peer connection.
     * Note that calling getSenders will dispose of the senders previously
     * returned.
     */
    public fun getSenders(): List<RtpSender>

    /**
     * Gets all RtpReceivers associated with this peer connection.
     * Note that calling getReceivers will dispose of the receivers previously
     * returned.
     */
    public fun getReceivers(): List<RtpReceiver>

    /**
     * Gets all RtpTransceivers associated with this peer connection.
     * Note that calling getTransceivers will dispose of the transceivers previously
     * returned.
     * Note: This is only available with SdpSemantics.UNIFIED_PLAN specified.
     */
    public fun getTransceivers(): List<RtpTransceiver>

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
    public fun addTrack(
        track: MediaStreamTrack,
        vararg streams: MediaStream,
    ): RtpSender

    /**
     * Stops sending media from sender. The sender will still appear in getSenders. Future
     * calls to createOffer will mark the m section for the corresponding transceiver as
     * receive only or inactive, as defined in JSEP. Returns true on success.
     */
    public fun removeTrack(sender: RtpSender): Boolean

    /**
     * Gets stats using the new stats collection API, see webrtc/api/stats/.
     */
    public suspend fun getStats(): RtcStatsReport?

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
    public fun close()
}

/**
 * Emits [PeerConnectionState] events. This happens whenever the aggregate state of the connection
 * changes.
 */
public val PeerConnection.onConnectionStateChange: Flow<PeerConnectionState>
    get() =
        peerConnectionEvent
            .map { it as? PeerConnectionEvent.ConnectionStateChange }
            .filterNotNull()
            .map { it.state }

/**
 * Emits [DataChannel] events. This event is sent when an [DataChannel] is added to the connection
 * by the remote peer calling [PeerConnection.createDataChannel]
 */
public val PeerConnection.onDataChannel: Flow<DataChannel>
    get() =
        peerConnectionEvent
            .map { it as? PeerConnectionEvent.NewDataChannel }
            .filterNotNull()
            .map { it.dataChannel }

/**
 * Emits [IceCandidate] events. This happens whenever the local ICE agent needs to deliver a message
 * to the other peer through the signaling server.
 */
public val PeerConnection.onIceCandidate: Flow<IceCandidate>
    get() =
        peerConnectionEvent
            .map { it as? PeerConnectionEvent.NewIceCandidate }
            .filterNotNull()
            .map { it.candidate }

/**
 * Emits [IceConnectionState] events. This happens when the state of the connection's ICE agent,
 * as represented by the [PeerConnection.iceConnectionState] property, changes.
 */
public val PeerConnection.onIceConnectionStateChange: Flow<IceConnectionState>
    get() =
        peerConnectionEvent
            .map { it as? PeerConnectionEvent.IceConnectionStateChange }
            .filterNotNull()
            .map { it.state }

/**
 * Emits [IceGatheringState] events. This happens when the ICE gathering state—that is, whether or
 * not the ICE agent is actively gathering candidates—changes.
 */
public val PeerConnection.onIceGatheringState: Flow<IceGatheringState>
    get() =
        peerConnectionEvent
            .map { it as? PeerConnectionEvent.IceGatheringStateChange }
            .filterNotNull()
            .map { it.state }

/**
 * Emits negotiationneeded events. This event is fired when a change has occurred which requires
 * session negotiation. This negotiation should be carried out as the offerer, because some session
 * changes cannot be negotiated as the answerer.
 */
public val PeerConnection.onNegotiationNeeded: Flow<Unit>
    get() =
        peerConnectionEvent
            .filter { it is PeerConnectionEvent.NegotiationNeeded }
            .map { }

/**
 * Emits [SignalingState] events..
 */
public val PeerConnection.onSignalingStateChange: Flow<SignalingState>
    get() =
        peerConnectionEvent
            .map { it as? PeerConnectionEvent.SignalingStateChange }
            .filterNotNull()
            .map { it.state }

/**
 * Emits track events, indicating that a track has been added to the [PeerConnection].
 */
public val PeerConnection.onTrack: Flow<TrackEvent>
    get() =
        peerConnectionEvent
            .map { it as? PeerConnectionEvent.Track }
            .filterNotNull()
            .map { it.trackEvent }

public val PeerConnection.onStandardizedIceConnection: Flow<IceConnectionState>
    get() =
        peerConnectionEvent
            .map { it as? PeerConnectionEvent.StandardizedIceConnectionChange }
            .filterNotNull()
            .map { it.state }

public val PeerConnection.onRemovedIceCandidates: Flow<List<IceCandidate>>
    get() =
        peerConnectionEvent
            .map { it as? PeerConnectionEvent.RemovedIceCandidates }
            .filterNotNull()
            .map { it.candidates }

public val PeerConnection.onRemoveTrack: Flow<RtpReceiver>
    get() =
        peerConnectionEvent
            .map { it as? PeerConnectionEvent.RemoveTrack }
            .filterNotNull()
            .map { it.rtpReceiver }

public enum class TlsCertPolicy { TlsCertPolicySecure, TlsCertPolicyInsecureNoCheck }

public enum class KeyType { RSA, ECDSA }

public enum class RtcpMuxPolicy { Negotiate, Require }

public enum class BundlePolicy { Balanced, MaxBundle, MaxCompat }

public enum class IceTransportPolicy { None, Relay, NoHost, All }

public enum class SignalingState {
    Stable,
    HaveLocalOffer,
    HaveLocalPranswer,
    HaveRemoteOffer,
    HaveRemotePranswer,
    Closed,
}

public enum class IceConnectionState {
    New,
    Checking,
    Connected,
    Completed,
    Failed,
    Disconnected,
    Closed,
    Count,
}

public enum class PeerConnectionState { New, Connecting, Connected, Disconnected, Failed, Closed; }

public enum class IceGatheringState { New, Gathering, Complete }

public enum class ContinualGatheringPolicy { GatherOnce, GatherContinually }
