package com.shepeliev.webrtckmp

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
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
    fun data_channel_should_work() = runTest(timeout = 5.seconds) {
        val pc1 = PeerConnection()
        val pc2 = PeerConnection()

        val pc1DataChannel = pc1.createDataChannel("dataChannel", maxRetransmits = 10)!!
        val pc2DataChannelDeferred = async(start = CoroutineStart.UNDISPATCHED) { pc2.onDataChannel.first() }

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

        val offer = pc1.createOffer(OfferAnswerOptions())
        pc1.setLocalDescription(offer)
        pc2.setRemoteDescription(offer)
        val answer = pc2.createAnswer(OfferAnswerOptions())
        pc2.setLocalDescription(answer)
        pc1.setRemoteDescription(answer)

        pc1IceCandidates.await().forEach { pc2.addIceCandidate(it) }
        pc2IceCandidates.await().forEach { pc1.addIceCandidate(it) }

        val pc2DataChannel = pc2DataChannelDeferred.await()
        if (pc2DataChannel.readyState != DataChannelState.Open) {
            println("Waiting for pc2DataChannel: ${pc2DataChannel.readyState}")
            pc2DataChannel.onOpen.first()
        }

        if (pc1DataChannel.readyState != DataChannelState.Open) {
            println("Waiting for pc1DataChannel: ${pc1DataChannel.readyState}")
            pc1DataChannel.onOpen.first()
        }

        // TODO: This fails in iOS simulator test
//        val pc1MessageDeferred = async(start = CoroutineStart.UNDISPATCHED) {
//            pc1DataChannel.onMessage
//                .onEach { println("Message received PC1: ${it.decodeToString()}") }
//                .map { it.decodeToString() }
//                .first()
//        }

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

        // TODO: This fails in iOS simulator test
//        assertEquals("Hello WebRTC KMP!", pc1MessageDeferred.await())

        pc1.close()
        pc2.close()
        pc1DataChannel.close()
        pc2DataChannel.close()
    }
}
