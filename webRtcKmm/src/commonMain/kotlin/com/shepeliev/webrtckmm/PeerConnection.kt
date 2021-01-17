package com.shepeliev.webrtckmm

import kotlin.jvm.JvmOverloads

expect class PeerConnection {
    fun addIceCandidate(candidate: IceCandidate)
    fun removeIceCandidates(candidates: List<IceCandidate>)
}

enum class SdpSemantics { PlanB, UnifiedPlan }
enum class PortPrunePolicy { NoPrune, PruneBasedOnPriority, KeepFirstReady }
enum class ContinualGatheringPolicy { GatherOnce, GatherContinually }
enum class TlsCertPolicy { TlsCertPolicySecure, TlsCertPolicyInsecureNoCheck }
enum class KeyType { RSA, ECDSA }
enum class CandidateNetworkPolicy { All, LowCost }
enum class TcpCandidatePolicy { Enabled, Disabled }
enum class RtcpMuxPolicy { Negotiate, Require }
enum class BundlePolicy { Balanced, Maxbundle, Maxcompat }
enum class IceTransportsType { None, Relay, Nohost, All }

enum class AdapterType(val bitMask: Int) {
    Unknown(0),
    Ethernet(1),
    WiFi(2),
    Cellular(4),
    Vpn(8),
    Loopback(16),
    AdapterTypeAny(32),
    Cellular2g(64),
    Cellular3g(128),
    Cellular4g(256),
    Cellular5g(512);
}


data class IceServer @JvmOverloads constructor(
    val urls: List<String>,
    val username: String = "",
    val password: String = "",
    val tlsCertPolicy: TlsCertPolicy = TlsCertPolicy.TlsCertPolicySecure,
    val hostname: String = "",
    val tlsAlpnProtocols: List<String>? = null,
    val tlsEllipticCurves: List<String>? = null
)

interface PeerConnectionObserver {
    fun onSignalingChange(newState: SignalingState)
    fun onIceConnectionChange(newState: IceConnectionState)
    fun onConnectionChange(newState: PeerConnectionState)
    fun onIceConnectionReceivingChange(receiving: Boolean)
    fun onIceGatheringChange(newState: IceGatheringState)
    fun onIceCandidate(candidate: IceCandidate)
    fun onIceCandidatesRemoved(candidates: List<IceCandidate>)
    fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent)
    fun onAddStream(stream: MediaStream)
    fun onRemoveStream(stream: MediaStream)
    fun onDataChannel(dataChannel: DataChannel)
    fun onRenegotiationNeeded()
    fun onAddTrack(receiver: RtpReceiver, streams: List<MediaStream>)
    fun onTrack(transceiver: RtpTransceiver)
}

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
    Closed;
}

enum class PeerConnectionState { New, Connecting, Connected, Disconnected, Failed, Closed; }

enum class IceGatheringState { New, Gathering, Complete }
