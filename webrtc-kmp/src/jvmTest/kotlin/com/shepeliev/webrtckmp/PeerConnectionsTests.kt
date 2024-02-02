package com.shepeliev.webrtckmp

import org.junit.Test
import kotlin.test.assertEquals

class PeerConnectionsTests {

    @Test
    fun testCreatePeerConnection() = runTest {
        val pc = PeerConnection()

        pc.close()
    }

    @Test
    fun createOffer() = runTest {
        val pc = PeerConnection()
        val offer: SessionDescription = pc.createOffer(OfferAnswerOptions())

        assertEquals(SessionDescriptionType.Offer, offer.type)

        pc.close()
    }

    @Test
    fun createAnswer() = runTest {
        val pc1 = PeerConnection()
        val pc2 = PeerConnection()

        val offer = pc1.createOffer(OfferAnswerOptions())
        pc1.setLocalDescription(offer)

        pc2.setRemoteDescription(offer)
        val answer = pc2.createAnswer(OfferAnswerOptions())
        pc2.setLocalDescription(answer)
        pc1.setRemoteDescription(answer)

        assertEquals(SessionDescriptionType.Answer, answer.type)

        pc1.close()
        pc2.close()
    }
}
