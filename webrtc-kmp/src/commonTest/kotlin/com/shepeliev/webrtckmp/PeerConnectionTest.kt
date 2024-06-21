package com.shepeliev.webrtckmp

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class PeerConnectionTest {
    private val scope = TestScope()

    @BeforeTest
    fun setUp() {
        setupMocks()
        Dispatchers.setMain(StandardTestDispatcher(scope.testScheduler))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testClose() {
        val pc = PeerConnection()
        pc.close()

        assertEquals(SignalingState.Closed, pc.signalingState, "Unexpected signaling state after close")
    }

    @Test
    fun testCreateDataChannel() {
        val pc = PeerConnection()
        val dataChannel1 = pc.createDataChannel("test1")
        val dataChannel2 = pc.createDataChannel("test2", id = 1)
        val dataChannel3 = pc.createDataChannel("test3", id = 1, maxRetransmits = 10)
        val dataChannel4 = pc.createDataChannel("test4", id = 1, maxPacketLifeTimeMs = 1000)
        val dataChannel6 = pc.createDataChannel("test5", maxRetransmits = 10)
        val dataChannel5 = pc.createDataChannel("test6", maxPacketLifeTimeMs = 1000)

        assertNotNull(dataChannel1)
        assertNotNull(dataChannel2)
        assertNotNull(dataChannel3)
        assertNotNull(dataChannel4)
        assertNotNull(dataChannel5)
        assertNotNull(dataChannel6)

        pc.close()
    }

    @Test
    fun testCreateOffer() = scope.runTest {
        val pc = PeerConnection()
        val offer = pc.createOffer(DefaultOfferAnswerOptions)

        assertEquals(SessionDescriptionType.Offer, offer.type)
        assertTrue(offer.sdp.isNotEmpty())

        pc.close()
    }

    @Test
    fun testCreateAnswer() = scope.runTest {
        val pc1 = PeerConnection()
        val pc2 = PeerConnection()
        val offer = pc1.createOffer(DefaultOfferAnswerOptions)

        pc2.setRemoteDescription(offer)
        val answer = pc2.createAnswer(DefaultOfferAnswerOptions)

        assertEquals(SessionDescriptionType.Answer, answer.type)
        assertTrue(answer.sdp.isNotEmpty())

        pc1.close()
        pc2.close()
    }

    @Test
    fun testSetLocalDescription() = scope.runTest {
        val pc = PeerConnection()
        val offer = pc.createOffer(DefaultOfferAnswerOptions)

        pc.setLocalDescription(offer)

        assertNotNull(pc.localDescription)

        pc.close()
    }

    @Test
    fun testSetLocalDescriptionRollback() = scope.runTest {
        val pc = PeerConnection()
        val offer = pc.createOffer(DefaultOfferAnswerOptions)
        pc.setLocalDescription(offer)

        pc.setLocalDescription(SessionDescription(SessionDescriptionType.Rollback, ""))

        assertNull(pc.localDescription)

        pc.close()
    }

    @Test
    fun testSetRemoteDescription() = scope.runTest {
        val pc1 = PeerConnection()
        val pc2 = PeerConnection()
        val offer = pc1.createOffer(DefaultOfferAnswerOptions)

        pc1.setLocalDescription(offer)
        pc2.setRemoteDescription(offer)

        assertEquals(offer, pc2.remoteDescription)

        pc1.close()
        pc2.close()
    }

    @Test
    fun testSetRemoteDescriptionRollback() = scope.runTest {
        val pc1 = PeerConnection()
        val pc2 = PeerConnection()
        val offer = pc1.createOffer(DefaultOfferAnswerOptions)

        pc1.setLocalDescription(offer)
        pc2.setRemoteDescription(offer)

        pc2.setRemoteDescription(SessionDescription(SessionDescriptionType.Rollback, ""))

        assertNull(pc2.remoteDescription)

        pc1.close()
        pc2.close()
    }

    @Test
    fun testSignalingStates() = scope.runTest {
        val pc1 = PeerConnection()
        val pc2 = PeerConnection()

        assertEquals(SignalingState.Stable, pc1.signalingState, "Unexpected initial signaling state")

        val offer = pc1.createOffer(DefaultOfferAnswerOptions)
        pc1.setLocalDescription(offer)
        assertEquals(
            SignalingState.HaveLocalOffer,
            pc1.signalingState,
            "Unexpected signaling state after offer creation"
        )

        pc2.setRemoteDescription(offer)
        val answer = pc2.createAnswer(DefaultOfferAnswerOptions)

        pc1.setRemoteDescription(answer)
        assertEquals(
            SignalingState.Stable,
            pc1.signalingState,
            "Unexpected signaling state after answer creation"
        )

        pc1.close()
        pc2.close()
    }

    @Test
    fun testPeerConnectionEvents() = scope.runTest(timeout = 5.seconds) {
        val pc1 = PeerConnection()
        val pc2 = PeerConnection()

        val iceConnectionStateEmitted = async(start = CoroutineStart.UNDISPATCHED) {
            pc1.onIceConnectionStateChange.first { it == IceConnectionState.Checking }
            true
        }

        val connectionStateChangeEmitted = async(start = CoroutineStart.UNDISPATCHED) {
            pc1.onConnectionStateChange.first { it == PeerConnectionState.Connecting }
            true
        }

        val signalingStateChangeEmitted = async(start = CoroutineStart.UNDISPATCHED) {
            pc1.onSignalingStateChange.first { it == SignalingState.HaveLocalOffer }
            true
        }

        val pc1IceCandidates = async(start = CoroutineStart.UNDISPATCHED) {
            val candidates = mutableListOf<IceCandidate>()
            val job = pc1.onIceCandidate.onEach { candidates += it }.launchIn(this)
            pc1.onIceGatheringState.first { it == IceGatheringState.Complete }
            job.cancel()
            candidates
        }

        val pc2IceCandidates = async(start = CoroutineStart.UNDISPATCHED) {
            val candidates = mutableListOf<IceCandidate>()
            val job = pc2.onIceCandidate.onEach { candidates += it }.launchIn(this)
            pc2.onIceGatheringState.first { it == IceGatheringState.Complete }
            job.cancel()
            candidates
        }

        val offer = pc1.createOffer(DefaultOfferAnswerOptions)
        pc1.setLocalDescription(offer)
        pc2.setRemoteDescription(offer)
        val answer = pc2.createAnswer(DefaultOfferAnswerOptions)
        pc2.setLocalDescription(answer)
        pc1.setRemoteDescription(answer)

        pc1IceCandidates.await().forEach { pc2.addIceCandidate(it) }
        pc2IceCandidates.await().forEach { pc1.addIceCandidate(it) }

        assertTrue(iceConnectionStateEmitted.await())
        assertTrue(connectionStateChangeEmitted.await())
        assertTrue(signalingStateChangeEmitted.await())

        pc1.close()
        pc2.close()
    }

    @Test
    fun testAddIceCandidate() = scope.runTest {
        val pc1 = PeerConnection()
        val pc2 = PeerConnection()

        val pc1IceCandidate = async { pc1.onIceCandidate.first() }
        val offer = pc1.createOffer(DefaultOfferAnswerOptions)
        pc1.setLocalDescription(offer)
        pc2.setRemoteDescription(offer)
        val answer = pc2.createAnswer(DefaultOfferAnswerOptions)
        pc2.setLocalDescription(answer)
        pc1.setRemoteDescription(answer)

        assertTrue(pc2.addIceCandidate(pc1IceCandidate.await()))

        pc1.close()
        pc2.close()
    }

    @Test
    fun testGetReceivers() = scope.runTest {
        val pc = PeerConnection()
        val offer = pc.createOffer(DefaultOfferAnswerOptions)
        pc.setLocalDescription(offer)

        assertTrue(pc.getReceivers().isNotEmpty())

        pc.close()
    }

    @Test
    fun testGetSenders() = scope.runTest {
        val pc = PeerConnection()
        val offer = pc.createOffer(DefaultOfferAnswerOptions)
        pc.setLocalDescription(offer)

        assertTrue(pc.getSenders().isNotEmpty())

        pc.close()
    }

    @Test
    fun testGetTransceivers() = scope.runTest {
        val pc = PeerConnection()
        val offer = pc.createOffer(DefaultOfferAnswerOptions)
        pc.setLocalDescription(offer)

        assertTrue(pc.getTransceivers().isNotEmpty())

        pc.close()
    }

    @Test
    @Ignore
    fun testAddTrack() = scope.runTest {
        val pc = PeerConnection()
        val offer = pc.createOffer(DefaultOfferAnswerOptions)
        pc.setLocalDescription(offer)

        assertTrue(pc.getSenders().isNotEmpty())

        pc.close()
    }

    @Test
    @Ignore
    fun testRemoveTrack() = scope.runTest {
        val pc = PeerConnection()
        val offer = pc.createOffer(DefaultOfferAnswerOptions)
        pc.setLocalDescription(offer)

        assertTrue(pc.getSenders().isNotEmpty())

        pc.close()
    }

    @Test
    @Ignore
    fun testGetStats() = scope.runTest {
        val pc = PeerConnection()
        val offer = pc.createOffer(DefaultOfferAnswerOptions)
        pc.setLocalDescription(offer)

        val stats = pc.getStats()
        assertNotNull(stats)

        pc.close()
    }
}

private val DefaultOfferAnswerOptions = OfferAnswerOptions(offerToReceiveAudio = true, offerToReceiveVideo = true)
