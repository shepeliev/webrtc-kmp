package com.shepeliev.webrtckmp

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class DataChannelTest {
    private val scope = TestScope()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher(scope.testScheduler))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSendAndReceiveData() = runTest(timeout = 5.seconds) {
        val pc1 = PeerConnection()
        val pc2 = PeerConnection()

        val pc1DataChannel = pc1.createDataChannel("dataChannel", maxRetransmits = 10)!!
        val pc2DataChannelDeferred = async(start = CoroutineStart.UNDISPATCHED) { pc2.onDataChannel.first() }

        connect(pc1, pc2)

        val pc2DataChannel = pc2DataChannelDeferred.await()
        pc1DataChannel.awaitOpen()
        pc2DataChannel.awaitOpen()

        val pc1MessageDeferred = async(start = CoroutineStart.UNDISPATCHED) {
            pc1DataChannel.onMessage
                .onEach { println("Message received PC1: ${it.decodeToString()}") }
                .map { it.decodeToString() }
                .first()
        }

        val pc2MessageDeferred = async(start = CoroutineStart.UNDISPATCHED) {
            pc2DataChannel.onMessage
                .onEach { println("Message received PC2: ${it.decodeToString()}") }
                .map { it.decodeToString() }
                .first()
        }

        val data = "Hello WebRTC KMP!".encodeToByteArray()

        assertTrue { pc1DataChannel.send(data) }
        assertTrue { pc2DataChannel.send(data) }

        assertEquals("Hello WebRTC KMP!", pc2MessageDeferred.await())
        assertEquals("Hello WebRTC KMP!", pc1MessageDeferred.await())

        pc1.close()
        pc2.close()
    }

    @Test
    fun testWithMultipleObservers() = runTest(timeout = 5.seconds) {
        val pc1 = PeerConnection()
        val pc2 = PeerConnection()

        val pc1DataChannel = pc1.createDataChannel("dataChannel", maxRetransmits = 10)!!
        val pc2DataChannelDeferred =
            async(start = CoroutineStart.UNDISPATCHED) { pc2.onDataChannel.first() }

        connect(pc1, pc2)

        val pc2DataChannel = pc2DataChannelDeferred.await()
        pc1DataChannel.awaitOpen()
        pc2DataChannel.awaitOpen()

        val messageFrom1stObserver = async(start = CoroutineStart.UNDISPATCHED) {
            pc2DataChannel.onMessage
                .onEach { withContext(Dispatchers.Default) { delay(200) } } // simulate some processing
                .map { it.decodeToString() }
                .first()
        }

        val messageFrom2ndObserver = async(start = CoroutineStart.UNDISPATCHED) {
            pc2DataChannel.onMessage
                .onEach { withContext(Dispatchers.Default) { delay(1000) } } // simulate some processing
                .map { it.decodeToString() }
                .first()
        }

        pc1DataChannel.send("Hello WebRTC KMP!".encodeToByteArray())

        assertEquals(messageFrom1stObserver.await(), messageFrom2ndObserver.await())
    }

    @Test
    fun testCloseEvents() = runTest(timeout = 5.seconds) {
        val pc1 = PeerConnection()
        val pc2 = PeerConnection()

        val pc1DataChannel = pc1.createDataChannel("dataChannel", maxRetransmits = 10)!!
        val pc2DataChannelDeferred =
            async(start = CoroutineStart.UNDISPATCHED) { pc2.onDataChannel.first() }

        connect(pc1, pc2)

        val pc2DataChannel = pc2DataChannelDeferred.await()
        pc1DataChannel.awaitOpen()
        pc2DataChannel.awaitOpen()

        val waitPc1ClosedJob = launch(start = CoroutineStart.UNDISPATCHED) {
            pc1DataChannel.onClose.first()
        }

        val waitPc2ClosedJob = launch(start = CoroutineStart.UNDISPATCHED) {
            pc2DataChannel.onClose.first()
        }

        pc1DataChannel.close()

        listOf(waitPc1ClosedJob, waitPc2ClosedJob).joinAll()
    }
}

// Candidates are trickled for as long as the test runs instead of being read from the local
// description after gathering completes. Since WebRTC M137 the Android network monitor serves
// its network list from a cache that is only filled by ConnectivityManager callbacks, so a peer
// connection created right after the monitor starts can see no networks, report ICE gathering
// as complete with no candidates, and only gather them a few milliseconds later.
private suspend fun TestScope.connect(
    pc1: PeerConnection,
    pc2: PeerConnection,
) {
    backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
        pc1.onIceCandidate.collect { pc2.addIceCandidate(it) }
    }
    backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
        pc2.onIceCandidate.collect { pc1.addIceCandidate(it) }
    }

    // Each remote description is set before the other side starts gathering, so no candidate
    // arrives at a peer that can't accept it yet.
    val offer = pc1.createOffer(OfferAnswerOptions())
    pc2.setRemoteDescription(offer)
    pc1.setLocalDescription(offer)

    val answer = pc2.createAnswer(OfferAnswerOptions())
    pc1.setRemoteDescription(answer)
    pc2.setLocalDescription(answer)
}

// The event flows don't replay, so subscribe before checking the state or a fast event is missed.
private suspend fun DataChannel.awaitOpen() = coroutineScope {
    val opened = async(start = CoroutineStart.UNDISPATCHED) { onOpen.first() }
    if (readyState == DataChannelState.Open) opened.cancel() else opened.await()
}
