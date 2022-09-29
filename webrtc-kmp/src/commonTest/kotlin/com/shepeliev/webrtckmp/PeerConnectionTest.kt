package com.shepeliev.webrtckmp

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PeerConnectionTest {

    @BeforeTest
    fun beforeTest() {
        WebRtc.initialize()
    }

    @AfterTest
    fun afterTest() {
        WebRtc.dispose()
    }

    @Test
    fun should_be_created_successfully() {
        assertNotNull(PeerConnection())
    }

    @Test
    fun should_be_closed_successfully() {
        val peerConnection = PeerConnection()
        peerConnection.close()
    }

    @Test
    fun should_create_data_channel() {
        val peerConnection = PeerConnection()
        val dataChannel = peerConnection.createDataChannel("test")

        assertNotNull(dataChannel)
    }

    @Test
    fun should_create_offer() = runTest {
        val peerConnection = PeerConnection()
        val offer = peerConnection.createOffer(DefaultOfferAnswerOptions)

        assertEquals(SessionDescriptionType.Offer, offer.type)
        assertTrue(offer.sdp.isNotEmpty())
    }

    @Test
    fun should_create_answer() = runTest {
        val peerConnection1 = PeerConnection()
        val peerConnection2 = PeerConnection()
        val offer = peerConnection1.createOffer(DefaultOfferAnswerOptions)

        peerConnection2.setRemoteDescription(offer)
        val answer = peerConnection2.createAnswer(DefaultOfferAnswerOptions)

        assertEquals(SessionDescriptionType.Answer, answer.type)
        assertTrue(answer.sdp.isNotEmpty())
    }

    @Test
    fun should_handle_signaling_state_properly() = runTest {
        val peerConnection1 = PeerConnection()
        val peerConnection2 = PeerConnection()

        assertEquals(SignalingState.Stable, peerConnection1.signalingState, "Unexpected initial signaling state")

        val offer = peerConnection1.createOffer(DefaultOfferAnswerOptions)
        peerConnection1.setLocalDescription(offer)
        assertEquals(
            SignalingState.HaveLocalOffer,
            peerConnection1.signalingState,
            "Unexpected signaling state after offer creation"
        )

        peerConnection2.setRemoteDescription(offer)
        val answer = peerConnection2.createAnswer(DefaultOfferAnswerOptions)

        peerConnection1.setRemoteDescription(answer)
        assertEquals(
            SignalingState.Stable,
            peerConnection1.signalingState,
            "Unexpected signaling state after answer creation"
        )
    }
}

private val DefaultOfferAnswerOptions = OfferAnswerOptions(offerToReceiveAudio = true, offerToReceiveVideo = true)
