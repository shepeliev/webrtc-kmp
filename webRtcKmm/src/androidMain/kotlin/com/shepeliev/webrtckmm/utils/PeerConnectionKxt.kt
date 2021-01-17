package com.shepeliev.webrtckmm.utils

import com.shepeliev.webrtckmm.AdapterType
import com.shepeliev.webrtckmm.BundlePolicy
import com.shepeliev.webrtckmm.CandidateNetworkPolicy
import com.shepeliev.webrtckmm.ContinualGatheringPolicy
import com.shepeliev.webrtckmm.CryptoOptions
import com.shepeliev.webrtckmm.IceConnectionState
import com.shepeliev.webrtckmm.IceGatheringState
import com.shepeliev.webrtckmm.IceServer
import com.shepeliev.webrtckmm.IceTransportsType
import com.shepeliev.webrtckmm.KeyType
import com.shepeliev.webrtckmm.PeerConnectionState
import com.shepeliev.webrtckmm.PortPrunePolicy
import com.shepeliev.webrtckmm.RtcConfiguration
import com.shepeliev.webrtckmm.RtcpMuxPolicy
import com.shepeliev.webrtckmm.SdpSemantics
import com.shepeliev.webrtckmm.SignalingState
import com.shepeliev.webrtckmm.TcpCandidatePolicy
import com.shepeliev.webrtckmm.TlsCertPolicy
import org.webrtc.PeerConnection

fun RtcConfiguration.toNative(): PeerConnection.RTCConfiguration {
    return PeerConnection.RTCConfiguration(iceServers.map { it.toNative() }).also {
        it.iceTransportsType = iceTransportsType.toNative()
        it.bundlePolicy = bundlePolicy.toNative()
        it.rtcpMuxPolicy = rtcpMuxPolicy.toNative()
        it.tcpCandidatePolicy = tcpCandidatePolicy.toNative()
        it.candidateNetworkPolicy = candidateNetworkPolicy.toNative()
        it.audioJitterBufferMaxPackets = audioJitterBufferMaxPackets
        it.audioJitterBufferFastAccelerate = audioJitterBufferFastAccelerate
        it.iceConnectionReceivingTimeout = iceConnectionReceivingTimeout
        it.iceBackupCandidatePairPingInterval = iceBackupCandidatePairPingInterval
        it.keyType = keyType.toNative()
        it.continualGatheringPolicy = continualGatheringPolicy.toNative()
        it.iceCandidatePoolSize = iceCandidatePoolSize
        it.turnPortPrunePolicy = turnPortPrunePolicy.toNative()
        it.presumeWritableWhenFullyRelayed = presumeWritableWhenFullyRelayed
        it.surfaceIceCandidatesOnIceTransportTypeChanged =
            surfaceIceCandidatesOnIceTransportTypeChanged
        it.iceCheckIntervalStrongConnectivityMs = iceCheckIntervalStrongConnectivityMs
        it.iceCheckIntervalWeakConnectivityMs = iceCheckIntervalWeakConnectivityMs
        it.iceCheckMinInterval = iceCheckMinInterval
        it.iceUnwritableTimeMs = iceUnwritableTimeMs
        it.iceUnwritableMinChecks = iceUnwritableMinChecks
        it.stunCandidateKeepaliveIntervalMs = stunCandidateKeepaliveIntervalMs
        it.disableIPv6OnWifi = disableIPv6OnWifi
        it.maxIPv6Networks = maxIPv6Networks
        it.disableIpv6 = disableIpv6
        it.enableDscp = enableDscp
        it.enableCpuOveruseDetection = enableCpuOveruseDetection
        it.enableRtpDataChannel = enableRtpDataChannel
        it.suspendBelowMinBitrate = suspendBelowMinBitrate
        it.screencastMinBitrate = screencastMinBitrate
        it.combinedAudioVideoBwe = combinedAudioVideoBwe
        it.enableDtlsSrtp = enableDtlsSrtp
        it.networkPreference = networkPreference.toNative()
        it.sdpSemantics = sdpSemantics.toNative()
        it.activeResetSrtpParams = activeResetSrtpParams
        it.cryptoOptions = cryptoOptions?.toNative()
        it.turnLoggingId = turnLoggingId
        it.allowCodecSwitching = allowCodecSwitching
    }
}

private fun CryptoOptions.toNative(): org.webrtc.CryptoOptions {
    return org.webrtc.CryptoOptions.builder()
        .setEnableAes128Sha1_32CryptoCipher(enableAes128Sha1_32CryptoCipher)
        .setEnableEncryptedRtpHeaderExtensions(enableEncryptedRtpHeaderExtensions)
        .setEnableGcmCryptoSuites(enableGcmCryptoSuites)
        .setRequireFrameEncryption(requireFrameEncryption)
        .createCryptoOptions()
}

private fun SdpSemantics.toNative(): PeerConnection.SdpSemantics {
    return when (this) {
        SdpSemantics.PlanB -> PeerConnection.SdpSemantics.PLAN_B
        SdpSemantics.UnifiedPlan -> PeerConnection.SdpSemantics.UNIFIED_PLAN
    }
}

private fun AdapterType.toNative(): PeerConnection.AdapterType {
    return when (this) {
        AdapterType.Unknown -> PeerConnection.AdapterType.UNKNOWN
        AdapterType.Ethernet -> PeerConnection.AdapterType.ETHERNET
        AdapterType.WiFi -> PeerConnection.AdapterType.WIFI
        AdapterType.Cellular -> PeerConnection.AdapterType.CELLULAR
        AdapterType.Vpn -> PeerConnection.AdapterType.VPN
        AdapterType.Loopback -> PeerConnection.AdapterType.LOOPBACK
        AdapterType.AdapterTypeAny -> PeerConnection.AdapterType.ADAPTER_TYPE_ANY
        AdapterType.Cellular2g -> PeerConnection.AdapterType.CELLULAR_2G
        AdapterType.Cellular3g -> PeerConnection.AdapterType.CELLULAR_3G
        AdapterType.Cellular4g -> PeerConnection.AdapterType.CELLULAR_4G
        AdapterType.Cellular5g -> PeerConnection.AdapterType.CELLULAR_5G
    }
}

private fun PortPrunePolicy.toNative(): PeerConnection.PortPrunePolicy {
    return when (this) {
        PortPrunePolicy.NoPrune -> PeerConnection.PortPrunePolicy.NO_PRUNE

        PortPrunePolicy.PruneBasedOnPriority -> {
            PeerConnection.PortPrunePolicy.PRUNE_BASED_ON_PRIORITY
        }

        PortPrunePolicy.KeepFirstReady -> PeerConnection.PortPrunePolicy.KEEP_FIRST_READY
    }
}

private fun ContinualGatheringPolicy.toNative(): PeerConnection.ContinualGatheringPolicy {
    return when (this) {
        ContinualGatheringPolicy.GatherOnce -> PeerConnection.ContinualGatheringPolicy.GATHER_ONCE
        ContinualGatheringPolicy.GatherContinually -> {
            PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }
    }
}

internal fun KeyType.toNative(): PeerConnection.KeyType {
    return when (this) {
        KeyType.RSA -> PeerConnection.KeyType.RSA
        KeyType.ECDSA -> PeerConnection.KeyType.ECDSA
    }
}

private fun CandidateNetworkPolicy.toNative(): PeerConnection.CandidateNetworkPolicy {
    return when (this) {
        CandidateNetworkPolicy.All -> PeerConnection.CandidateNetworkPolicy.ALL
        CandidateNetworkPolicy.LowCost -> PeerConnection.CandidateNetworkPolicy.LOW_COST
    }
}

private fun TcpCandidatePolicy.toNative(): PeerConnection.TcpCandidatePolicy {
    return when (this) {
        TcpCandidatePolicy.Enabled -> PeerConnection.TcpCandidatePolicy.ENABLED
        TcpCandidatePolicy.Disabled -> PeerConnection.TcpCandidatePolicy.DISABLED
    }
}

private fun RtcpMuxPolicy.toNative(): PeerConnection.RtcpMuxPolicy {
    return when (this) {
        RtcpMuxPolicy.Negotiate -> PeerConnection.RtcpMuxPolicy.NEGOTIATE
        RtcpMuxPolicy.Require -> PeerConnection.RtcpMuxPolicy.REQUIRE
    }
}

private fun BundlePolicy.toNative(): PeerConnection.BundlePolicy {
    return when (this) {
        BundlePolicy.Balanced -> PeerConnection.BundlePolicy.BALANCED
        BundlePolicy.Maxbundle -> PeerConnection.BundlePolicy.MAXBUNDLE
        BundlePolicy.Maxcompat -> PeerConnection.BundlePolicy.MAXCOMPAT
    }
}

private fun IceTransportsType.toNative(): PeerConnection.IceTransportsType {
    return when (this) {
        IceTransportsType.None -> PeerConnection.IceTransportsType.NONE
        IceTransportsType.Relay -> PeerConnection.IceTransportsType.RELAY
        IceTransportsType.Nohost -> PeerConnection.IceTransportsType.NOHOST
        IceTransportsType.All -> PeerConnection.IceTransportsType.ALL
    }
}

fun TlsCertPolicy.toNative(): PeerConnection.TlsCertPolicy {
    return when (this) {
        TlsCertPolicy.TlsCertPolicySecure -> PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_SECURE

        TlsCertPolicy.TlsCertPolicyInsecureNoCheck -> {
            PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK
        }
    }
}

fun IceServer.toNative(): PeerConnection.IceServer {
    return PeerConnection.IceServer.builder(urls)
        .setUsername(username)
        .setPassword(password)
        .setTlsCertPolicy(tlsCertPolicy.toNative())
        .setHostname(hostname)
        .setTlsAlpnProtocols(tlsAlpnProtocols)
        .setTlsEllipticCurves(tlsEllipticCurves)
        .createIceServer()
}

fun PeerConnection.SignalingState.toCommon(): SignalingState {
    return when(this) {
        PeerConnection.SignalingState.STABLE -> SignalingState.Stable
        PeerConnection.SignalingState.HAVE_LOCAL_OFFER -> SignalingState.HaveLocalOffer
        PeerConnection.SignalingState.HAVE_LOCAL_PRANSWER -> SignalingState.HaveLocalPranswer
        PeerConnection.SignalingState.HAVE_REMOTE_OFFER -> SignalingState.HaveRemoteOffer
        PeerConnection.SignalingState.HAVE_REMOTE_PRANSWER -> SignalingState.HaveRemotePranswer
        PeerConnection.SignalingState.CLOSED -> SignalingState.Closed
    }
}

fun PeerConnection.IceConnectionState.toCommon(): IceConnectionState {
    return when(this) {
        PeerConnection.IceConnectionState.NEW -> IceConnectionState.New
        PeerConnection.IceConnectionState.CHECKING -> IceConnectionState.Checking
        PeerConnection.IceConnectionState.CONNECTED -> IceConnectionState.Connected
        PeerConnection.IceConnectionState.COMPLETED -> IceConnectionState.Completed
        PeerConnection.IceConnectionState.FAILED -> IceConnectionState.Failed
        PeerConnection.IceConnectionState.DISCONNECTED -> IceConnectionState.Disconnected
        PeerConnection.IceConnectionState.CLOSED -> IceConnectionState.Closed
    }
}

fun PeerConnection.PeerConnectionState.toCommon(): PeerConnectionState {
    return when(this) {
        PeerConnection.PeerConnectionState.NEW -> PeerConnectionState.New
        PeerConnection.PeerConnectionState.CONNECTING -> PeerConnectionState.Connecting
        PeerConnection.PeerConnectionState.CONNECTED -> PeerConnectionState.Connected
        PeerConnection.PeerConnectionState.DISCONNECTED -> PeerConnectionState.Disconnected
        PeerConnection.PeerConnectionState.FAILED -> PeerConnectionState.Failed
        PeerConnection.PeerConnectionState.CLOSED -> PeerConnectionState.Closed
    }
}

fun PeerConnection.IceGatheringState.toCommon(): IceGatheringState {
    return when(this) {
        PeerConnection.IceGatheringState.NEW -> IceGatheringState.New
        PeerConnection.IceGatheringState.GATHERING -> IceGatheringState.Gathering
        PeerConnection.IceGatheringState.COMPLETE -> IceGatheringState.Complete
    }
}

fun PeerConnection.AdapterType.toCommon(): AdapterType {
    return when(this) {
        PeerConnection.AdapterType.UNKNOWN -> AdapterType.Unknown
        PeerConnection.AdapterType.ETHERNET -> AdapterType.Ethernet
        PeerConnection.AdapterType.WIFI -> AdapterType.WiFi
        PeerConnection.AdapterType.CELLULAR -> AdapterType.Cellular
        PeerConnection.AdapterType.VPN -> AdapterType.Vpn
        PeerConnection.AdapterType.LOOPBACK -> AdapterType.Loopback
        PeerConnection.AdapterType.ADAPTER_TYPE_ANY -> AdapterType.AdapterTypeAny
        PeerConnection.AdapterType.CELLULAR_2G -> AdapterType.Cellular2g
        PeerConnection.AdapterType.CELLULAR_3G -> AdapterType.Cellular3g
        PeerConnection.AdapterType.CELLULAR_4G -> AdapterType.Cellular4g
        PeerConnection.AdapterType.CELLULAR_5G -> AdapterType.Cellular5g
    }
}
