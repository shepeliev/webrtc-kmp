package com.shepeliev.webrtckmp

import org.webrtc.PeerConnection

actual class RtcConfiguration private constructor(val native: PeerConnection.RTCConfiguration) {
    actual constructor(
        iceServers: List<IceServer>,
        iceTransportsType: IceTransportsType,
        bundlePolicy: BundlePolicy,
        certificate: RtcCertificatePem?,
        rtcpMuxPolicy: RtcpMuxPolicy,
        tcpCandidatePolicy: TcpCandidatePolicy,
        candidateNetworkPolicy: CandidateNetworkPolicy,
        audioJitterBufferMaxPackets: Int,
        audioJitterBufferFastAccelerate: Boolean,
        iceConnectionReceivingTimeout: Int,
        iceBackupCandidatePairPingInterval: Int,
        keyType: KeyType,
        continualGatheringPolicy: ContinualGatheringPolicy,
        iceCandidatePoolSize: Int,
        turnPortPrunePolicy: PortPrunePolicy,
        presumeWritableWhenFullyRelayed: Boolean,
        surfaceIceCandidatesOnIceTransportTypeChanged: Boolean,
        iceCheckIntervalStrongConnectivityMs: Int?,
        iceCheckIntervalWeakConnectivityMs: Int?,
        iceCheckMinInterval: Int?,
        iceUnwritableTimeMs: Int?,
        iceUnwritableMinChecks: Int?,
        stunCandidateKeepaliveIntervalMs: Int?,
        disableIpv6OnWifi: Boolean,
        maxIPv6Networks: Int,
        disableIpv6: Boolean,
        enableDscp: Boolean,
        enableCpuOveruseDetection: Boolean,
        enableRtpDataChannel: Boolean,
        suspendBelowMinBitrate: Boolean,
        screencastMinBitrate: Int?,
        combinedAudioVideoBwe: Boolean?,
        enableDtlsSrtp: Boolean?,
        networkPreference: AdapterType,
        sdpSemantics: SdpSemantics,
        activeResetSrtpParams: Boolean,
        allowCodecSwitching: Boolean?,
        cryptoOptions: CryptoOptions?,
        turnLoggingId: String?,
    ) : this(
        PeerConnection.RTCConfiguration(iceServers.map { it.native }).also {
            it.iceTransportsType = iceTransportsType.asNative()
            it.bundlePolicy = bundlePolicy.asNative()
            it.certificate = certificate?.native
            it.rtcpMuxPolicy = rtcpMuxPolicy.asNative()
            it.tcpCandidatePolicy = tcpCandidatePolicy.asNative()
            it.candidateNetworkPolicy = candidateNetworkPolicy.asNative()
            it.audioJitterBufferMaxPackets = audioJitterBufferMaxPackets
            it.audioJitterBufferFastAccelerate = audioJitterBufferFastAccelerate
            it.iceConnectionReceivingTimeout = iceConnectionReceivingTimeout
            it.iceBackupCandidatePairPingInterval = iceBackupCandidatePairPingInterval
            it.keyType = keyType.asNative()
            it.continualGatheringPolicy = continualGatheringPolicy.asNative()
            it.iceCandidatePoolSize = iceCandidatePoolSize
            it.turnPortPrunePolicy = turnPortPrunePolicy.asNative()
            it.presumeWritableWhenFullyRelayed = presumeWritableWhenFullyRelayed
            it.surfaceIceCandidatesOnIceTransportTypeChanged =
                surfaceIceCandidatesOnIceTransportTypeChanged
            it.iceCheckIntervalStrongConnectivityMs = iceCheckIntervalStrongConnectivityMs
            it.iceCheckIntervalWeakConnectivityMs = iceCheckIntervalWeakConnectivityMs
            it.iceCheckMinInterval = iceCheckMinInterval
            it.iceUnwritableTimeMs = iceUnwritableTimeMs
            it.iceUnwritableMinChecks = iceUnwritableMinChecks
            it.stunCandidateKeepaliveIntervalMs = stunCandidateKeepaliveIntervalMs
            it.disableIPv6OnWifi = disableIpv6OnWifi
            it.maxIPv6Networks = maxIPv6Networks
            it.disableIpv6 = disableIpv6
            it.enableDscp = enableDscp
            it.enableCpuOveruseDetection = enableCpuOveruseDetection
            it.enableRtpDataChannel = enableRtpDataChannel
            it.suspendBelowMinBitrate = suspendBelowMinBitrate
            it.screencastMinBitrate = screencastMinBitrate
            it.combinedAudioVideoBwe = combinedAudioVideoBwe
            it.enableDtlsSrtp = enableDtlsSrtp
            it.networkPreference = networkPreference.asNative()
            it.sdpSemantics = sdpSemantics.asNative()
            it.activeResetSrtpParams = activeResetSrtpParams
            it.allowCodecSwitching = allowCodecSwitching
            it.cryptoOptions = cryptoOptions?.native
            it.turnLoggingId = turnLoggingId
        }
    )
}

private fun SdpSemantics.asNative(): PeerConnection.SdpSemantics {
    return when (this) {
        SdpSemantics.PlanB -> PeerConnection.SdpSemantics.PLAN_B
        SdpSemantics.UnifiedPlan -> PeerConnection.SdpSemantics.UNIFIED_PLAN
    }
}

private fun AdapterType.asNative(): PeerConnection.AdapterType {
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

private fun PortPrunePolicy.asNative(): PeerConnection.PortPrunePolicy {
    return when (this) {
        PortPrunePolicy.NoPrune -> PeerConnection.PortPrunePolicy.NO_PRUNE

        PortPrunePolicy.PruneBasedOnPriority -> {
            PeerConnection.PortPrunePolicy.PRUNE_BASED_ON_PRIORITY
        }

        PortPrunePolicy.KeepFirstReady -> PeerConnection.PortPrunePolicy.KEEP_FIRST_READY
    }
}

private fun ContinualGatheringPolicy.asNative(): PeerConnection.ContinualGatheringPolicy {
    return when (this) {
        ContinualGatheringPolicy.GatherOnce -> PeerConnection.ContinualGatheringPolicy.GATHER_ONCE
        ContinualGatheringPolicy.GatherContinually -> {
            PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }
    }
}

internal fun KeyType.asNative(): PeerConnection.KeyType {
    return when (this) {
        KeyType.RSA -> PeerConnection.KeyType.RSA
        KeyType.ECDSA -> PeerConnection.KeyType.ECDSA
    }
}

private fun CandidateNetworkPolicy.asNative(): PeerConnection.CandidateNetworkPolicy {
    return when (this) {
        CandidateNetworkPolicy.All -> PeerConnection.CandidateNetworkPolicy.ALL
        CandidateNetworkPolicy.LowCost -> PeerConnection.CandidateNetworkPolicy.LOW_COST
    }
}

private fun TcpCandidatePolicy.asNative(): PeerConnection.TcpCandidatePolicy {
    return when (this) {
        TcpCandidatePolicy.Enabled -> PeerConnection.TcpCandidatePolicy.ENABLED
        TcpCandidatePolicy.Disabled -> PeerConnection.TcpCandidatePolicy.DISABLED
    }
}

private fun RtcpMuxPolicy.asNative(): PeerConnection.RtcpMuxPolicy {
    return when (this) {
        RtcpMuxPolicy.Negotiate -> PeerConnection.RtcpMuxPolicy.NEGOTIATE
        RtcpMuxPolicy.Require -> PeerConnection.RtcpMuxPolicy.REQUIRE
    }
}

private fun BundlePolicy.asNative(): PeerConnection.BundlePolicy {
    return when (this) {
        BundlePolicy.Balanced -> PeerConnection.BundlePolicy.BALANCED
        BundlePolicy.Maxbundle -> PeerConnection.BundlePolicy.MAXBUNDLE
        BundlePolicy.Maxcompat -> PeerConnection.BundlePolicy.MAXCOMPAT
    }
}

private fun IceTransportsType.asNative(): PeerConnection.IceTransportsType {
    return when (this) {
        IceTransportsType.None -> PeerConnection.IceTransportsType.NONE
        IceTransportsType.Relay -> PeerConnection.IceTransportsType.RELAY
        IceTransportsType.Nohost -> PeerConnection.IceTransportsType.NOHOST
        IceTransportsType.All -> PeerConnection.IceTransportsType.ALL
    }
}
