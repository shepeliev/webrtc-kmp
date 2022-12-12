package com.shepeliev.webrtckmp

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.onEach
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IceServerTests {

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

        val mediaDevices = MediaDevices.getUserMedia {
            audio()
        }

        mediaDevices.tracks.forEach {
            pc1.addTrack(it)
        }

        val offer = pc1.createOffer(OfferAnswerOptions())
        pc1.setLocalDescription(offer)

        pc2.setRemoteDescription(offer)

        val answer = pc2.createAnswer(OfferAnswerOptions())
        pc2.setLocalDescription(answer)

        pc1.setRemoteDescription(answer)

        val event = pc1.peerConnectionEvent.first { it is PeerConnectionEvent.NewIceCandidate }

        assertTrue(event is PeerConnectionEvent.NewIceCandidate)

        pc1.close()
        pc2.close()
    }

}