package com.shepeliev.webrtckmp

import WebRTC.RTCBundlePolicy
import WebRTC.RTCCandidateNetworkPolicy
import WebRTC.RTCConfiguration
import WebRTC.RTCContinualGatheringPolicy
import WebRTC.RTCEncryptionKeyType
import WebRTC.RTCRtcpMuxPolicy
import WebRTC.RTCSdpSemantics
import WebRTC.RTCTcpCandidatePolicy
import WebRTC.RTCTlsCertPolicy
import platform.Foundation.NSNumber

actual class RtcConfiguration actual constructor(
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
    turnLoggingId: String?
) {

    val native: RTCConfiguration
    val enableDtlsSrtp: Boolean?
    val enableRtpDataChannel: Boolean

    init {
        this.enableDtlsSrtp = enableDtlsSrtp
        this.enableRtpDataChannel = enableRtpDataChannel

        native = RTCConfiguration().also {
            it.iceServers = iceServers.map(IceServer::native)
//            it.iceTransportsType = iceTransportsType.asNative()
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
//            it.turnPortPrunePolicy = turnPortPrunePolicy.asNative()
            it.shouldPruneTurnPorts = turnPortPrunePolicy != PortPrunePolicy.NoPrune
            it.shouldPresumeWritableWhenFullyRelayed = presumeWritableWhenFullyRelayed
//            it.surfaceIceCandidatesOnIceTransportTypeChanged =
//                surfaceIceCandidatesOnIceTransportTypeChanged
//            it.iceCheckIntervalStrongConnectivityMs = iceCheckIntervalStrongConnectivityMs
//            it.iceCheckIntervalWeakConnectivityMs = iceCheckIntervalWeakConnectivityMs
            it.iceCheckMinInterval = iceCheckMinInterval?.let { NSNumber(it) }
//            it.iceUnwritableTimeMs = iceUnwritableTimeMs
//            it.iceUnwritableMinChecks = iceUnwritableMinChecks
//            it.stunCandidateKeepaliveIntervalMs = stunCandidateKeepaliveIntervalMs
            it.disableIPV6OnWiFi = disableIpv6OnWifi
            it.maxIPv6Networks = maxIPv6Networks
            it.disableIPV6 = disableIpv6
//            it.enableDscp = enableDscp
//            it.enableCpuOveruseDetection = enableCpuOveruseDetection
//            it.enableRtpDataChannel = enableRtpDataChannel
//            it.suspendBelowMinBitrate = suspendBelowMinBitrate
//            it.screencastMinBitrate = screencastMinBitrate
//            it.combinedAudioVideoBwe = combinedAudioVideoBwe
//            it.enableDtlsSrtp = enableDtlsSrtp
//            it.networkPreference = networkPreference.asNative()
            it.sdpSemantics = sdpSemantics.asNative()
            it.activeResetSrtpParams = activeResetSrtpParams
//            it.allowCodecSwitching = allowCodecSwitching
            it.cryptoOptions = cryptoOptions?.native
//            it.turnLoggingId = turnLoggingId
        }
    }
}

internal fun TlsCertPolicy.asNative(): RTCTlsCertPolicy {
    return when (this) {
        TlsCertPolicy.TlsCertPolicySecure -> RTCTlsCertPolicy.RTCTlsCertPolicySecure

        TlsCertPolicy.TlsCertPolicyInsecureNoCheck -> {
            RTCTlsCertPolicy.RTCTlsCertPolicyInsecureNoCheck
        }
    }
}

private fun SdpSemantics.asNative(): RTCSdpSemantics {
    return when (this) {
        SdpSemantics.PlanB -> RTCSdpSemantics.RTCSdpSemanticsPlanB
        SdpSemantics.UnifiedPlan -> RTCSdpSemantics.RTCSdpSemanticsUnifiedPlan
    }
}

private fun ContinualGatheringPolicy.asNative(): RTCContinualGatheringPolicy {
    return when (this) {
        ContinualGatheringPolicy.GatherOnce -> {
            RTCContinualGatheringPolicy.RTCContinualGatheringPolicyGatherOnce
        }

        ContinualGatheringPolicy.GatherContinually -> {
            RTCContinualGatheringPolicy.RTCContinualGatheringPolicyGatherContinually
        }
    }
}

internal fun KeyType.asNative(): RTCEncryptionKeyType {
    return when (this) {
        KeyType.RSA -> RTCEncryptionKeyType.RTCEncryptionKeyTypeRSA
        KeyType.ECDSA -> RTCEncryptionKeyType.RTCEncryptionKeyTypeECDSA
    }
}

private fun CandidateNetworkPolicy.asNative(): RTCCandidateNetworkPolicy {
    return when (this) {
        CandidateNetworkPolicy.All -> RTCCandidateNetworkPolicy.RTCCandidateNetworkPolicyAll
        CandidateNetworkPolicy.LowCost -> RTCCandidateNetworkPolicy.RTCCandidateNetworkPolicyLowCost
    }
}

private fun TcpCandidatePolicy.asNative(): RTCTcpCandidatePolicy {
    return when (this) {
        TcpCandidatePolicy.Enabled -> RTCTcpCandidatePolicy.RTCTcpCandidatePolicyEnabled
        TcpCandidatePolicy.Disabled -> RTCTcpCandidatePolicy.RTCTcpCandidatePolicyDisabled
    }
}

private fun RtcpMuxPolicy.asNative(): RTCRtcpMuxPolicy {
    return when (this) {
        RtcpMuxPolicy.Negotiate -> RTCRtcpMuxPolicy.RTCRtcpMuxPolicyNegotiate
        RtcpMuxPolicy.Require -> RTCRtcpMuxPolicy.RTCRtcpMuxPolicyRequire
    }
}

private fun BundlePolicy.asNative(): RTCBundlePolicy {
    return when (this) {
        BundlePolicy.Balanced -> RTCBundlePolicy.RTCBundlePolicyBalanced
        BundlePolicy.Maxbundle -> RTCBundlePolicy.RTCBundlePolicyMaxBundle
        BundlePolicy.Maxcompat -> RTCBundlePolicy.RTCBundlePolicyMaxCompat
    }
}
