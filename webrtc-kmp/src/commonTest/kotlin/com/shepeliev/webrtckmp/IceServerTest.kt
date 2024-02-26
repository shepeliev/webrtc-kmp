package com.shepeliev.webrtckmp

import kotlinx.coroutines.flow.first
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class IceServerTest {

    @Test
    fun should_build_successfully() {
        IceServer(
            urls = listOf("stun:url.to.stun.com"),
            username = "username",
            password = "password",
            tlsCertPolicy = TlsCertPolicy.TlsCertPolicySecure,
        )
    }

    @Test
    fun shouldGatherCandidates() = runTest {
        val pc1 = PeerConnection(
            rtcConfiguration = RtcConfiguration(
                iceServers = listOf(
                    IceServer(
                        urls = listOf(
                            "stun:stun.l.google.com:19302",
                            "stun:stun1.l.google.com:19302",
                            "stun:stun2.l.google.com:19302",
                            "stun:stun3.l.google.com:19302",
                            "stun:stun4.l.google.com:19302",
                        ),
                    )
                )
            )
        )
        val pc2 = PeerConnection(
            rtcConfiguration = RtcConfiguration(
                iceServers = listOf(
                    IceServer(
                        urls = listOf(
                            "stun:stun.l.google.com:19302",
                            "stun:stun1.l.google.com:19302",
                            "stun:stun2.l.google.com:19302",
                            "stun:stun3.l.google.com:19302",
                            "stun:stun4.l.google.com:19302",
                        ),
                    )
                )
            )
        )

        val dc = pc1.createDataChannel("Data")

        val offer = pc1.createOffer(OfferAnswerOptions())
        pc1.setLocalDescription(offer)

        pc2.setRemoteDescription(offer)

        val answer = pc2.createAnswer(OfferAnswerOptions())
        pc2.setLocalDescription(answer)

        pc1.setRemoteDescription(answer)

        val event = pc1.peerConnectionEvent.first { it is PeerConnectionEvent.NewIceCandidate }

        assertTrue(event is PeerConnectionEvent.NewIceCandidate)

        dc?.close()
        pc1.close()
        pc2.close()
    }
}
