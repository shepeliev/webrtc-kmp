package com.shepeliev.webrtckmm

import org.webrtc.PeerConnection

internal fun TlsCertPolicy.toNative(): PeerConnection.TlsCertPolicy {
    return when (this) {
        TlsCertPolicy.TlsCertPolicySecure -> PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_SECURE

        TlsCertPolicy.TlsCertPolicyInsecureNoCheck -> {
            PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK
        }
    }
}

internal fun IceServer.toNative(): PeerConnection.IceServer {
    return PeerConnection.IceServer.builder(urls)
        .setUsername(username)
        .setPassword(password)
        .setTlsCertPolicy(tlsCertPolicy.toNative())
        .setHostname(hostname)
        .setTlsAlpnProtocols(tlsAlpnProtocols)
        .setTlsEllipticCurves(tlsEllipticCurves)
        .createIceServer()
}

internal fun PeerConnection.SignalingState.toCommon(): SignalingState {
    return when(this) {
        PeerConnection.SignalingState.STABLE -> SignalingState.Stable
        PeerConnection.SignalingState.HAVE_LOCAL_OFFER -> SignalingState.HaveLocalOffer
        PeerConnection.SignalingState.HAVE_LOCAL_PRANSWER -> SignalingState.HaveLocalPranswer
        PeerConnection.SignalingState.HAVE_REMOTE_OFFER -> SignalingState.HaveRemoteOffer
        PeerConnection.SignalingState.HAVE_REMOTE_PRANSWER -> SignalingState.HaveRemotePranswer
        PeerConnection.SignalingState.CLOSED -> SignalingState.Closed
    }
}

internal fun PeerConnection.IceConnectionState.toCommon(): IceConnectionState {
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

internal fun PeerConnection.PeerConnectionState.toCommon(): PeerConnectionState {
    return when(this) {
        PeerConnection.PeerConnectionState.NEW -> PeerConnectionState.New
        PeerConnection.PeerConnectionState.CONNECTING -> PeerConnectionState.Connecting
        PeerConnection.PeerConnectionState.CONNECTED -> PeerConnectionState.Connected
        PeerConnection.PeerConnectionState.DISCONNECTED -> PeerConnectionState.Disconnected
        PeerConnection.PeerConnectionState.FAILED -> PeerConnectionState.Failed
        PeerConnection.PeerConnectionState.CLOSED -> PeerConnectionState.Closed
    }
}

internal fun PeerConnection.IceGatheringState.toCommon(): IceGatheringState {
    return when(this) {
        PeerConnection.IceGatheringState.NEW -> IceGatheringState.New
        PeerConnection.IceGatheringState.GATHERING -> IceGatheringState.Gathering
        PeerConnection.IceGatheringState.COMPLETE -> IceGatheringState.Complete
    }
}

internal fun PeerConnection.AdapterType.toCommon(): AdapterType {
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
