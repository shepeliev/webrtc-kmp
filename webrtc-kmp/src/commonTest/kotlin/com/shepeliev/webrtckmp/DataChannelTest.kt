package com.shepeliev.webrtckmp

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
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

        val offer = pc1.createOffer(OfferAnswerOptions())
        pc1.setLocalDescription(offer)
        pc1.onIceGatheringState.first { it == IceGatheringState.Complete }
        pc2.setRemoteDescription(pc1.localDescription!!)

        val answer = pc2.createAnswer(OfferAnswerOptions())
        pc2.setLocalDescription(answer)
        pc2.onIceGatheringState.first { it == IceGatheringState.Complete }
        pc1.setRemoteDescription(pc2.localDescription!!)

        val pc2DataChannel = pc2DataChannelDeferred.await()
        if (pc2DataChannel.readyState != DataChannelState.Open) {
            pc2DataChannel.onOpen.first()
        }

        if (pc1DataChannel.readyState != DataChannelState.Open) {
            pc1DataChannel.onOpen.first()
        }

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
        pc1DataChannel.close()
        pc2DataChannel.close()
    }

    @Test
    fun testWithMultipleObservers() = runTest(timeout = 5.seconds) {
        val pc1 = PeerConnection()
        val pc2 = PeerConnection()

        val pc1DataChannel = pc1.createDataChannel("dataChannel", maxRetransmits = 10)!!
        val pc2DataChannelDeferred =
            async(start = CoroutineStart.UNDISPATCHED) { pc2.onDataChannel.first() }

        val offer = pc1.createOffer(OfferAnswerOptions())
        pc1.setLocalDescription(offer)
        pc1.onIceGatheringState.first { it == IceGatheringState.Complete }
        pc2.setRemoteDescription(pc1.localDescription!!)

        val answer = pc2.createAnswer(OfferAnswerOptions())
        pc2.setLocalDescription(answer)
        pc2.onIceGatheringState.first { it == IceGatheringState.Complete }
        pc1.setRemoteDescription(pc2.localDescription!!)

        val pc2DataChannel = pc2DataChannelDeferred.await()
        if (pc2DataChannel.readyState != DataChannelState.Open) {
            pc2DataChannel.onOpen.first()
        }

        if (pc1DataChannel.readyState != DataChannelState.Open) {
            pc1DataChannel.onOpen.first()
        }

        val messageFrom1stObserver = async(start = CoroutineStart.UNDISPATCHED) {
            pc2DataChannel.onMessage
                .onEach { delay(200) } // simulate some processing
                .map { it.decodeToString() }
                .first()
        }

        val messageFrom2ndObserver = async(start = CoroutineStart.UNDISPATCHED) {
            pc2DataChannel.onMessage
                .onEach { delay(1000) } // simulate some processing
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

        val offer = pc1.createOffer(OfferAnswerOptions())
        pc1.setLocalDescription(offer)
        pc1.onIceGatheringState.first { it == IceGatheringState.Complete }
        pc2.setRemoteDescription(pc1.localDescription!!)

        val answer = pc2.createAnswer(OfferAnswerOptions())
        pc2.setLocalDescription(answer)
        pc2.onIceGatheringState.first { it == IceGatheringState.Complete }
        pc1.setRemoteDescription(pc2.localDescription!!)

        val pc2DataChannel = pc2DataChannelDeferred.await()
        if (pc2DataChannel.readyState != DataChannelState.Open) {
            pc2DataChannel.onOpen.first()
        }

        if (pc1DataChannel.readyState != DataChannelState.Open) {
            pc1DataChannel.onOpen.first()
        }

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
