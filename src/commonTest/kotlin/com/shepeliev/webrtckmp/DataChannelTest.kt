package com.shepeliev.webrtckmp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withTimeout
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DataChannelTest {

    private val appScope: CoroutineScope = MainScope()

    @BeforeTest
    fun setUp() {
        initialize()
    }

    @Test
    fun data_channel_should_work() = runTest {
        val pc1 = PeerConnection()
        val pc2 = PeerConnection()

        val dataChannel = pc1.createDataChannel("dataChannel")!!
        val pc1Candidates = mutableListOf<IceCandidate>()
        val pc2Candidates = mutableListOf<IceCandidate>()

        pc1.onIceCandidate
            .onEach { candidate ->
                if (pc2.signalingState == SignalingState.Stable) {
                    pc1Candidates.forEach { pc2.addIceCandidate(it) }
                    pc1Candidates.clear()
                    pc2.addIceCandidate(candidate)
                } else {
                    pc1Candidates += candidate
                }
            }
            .launchIn(appScope)

        pc2.onIceCandidate
            .onEach { candidate ->
                if (pc1.signalingState == SignalingState.Stable) {
                    pc2Candidates.forEach { pc1.addIceCandidate(it) }
                    pc2Candidates.clear()
                    pc1.addIceCandidate(candidate)
                } else {
                    pc2Candidates += candidate
                }
            }
            .launchIn(appScope)
        pc2.onDataChannel
            .onEach { pc2DataChannel ->
                val data = "Hello WebRTC KMP!".encodeToByteArray()
                pc2DataChannel.send(data)
            }
            .launchIn(appScope)

        val offer = pc1.createOffer(OfferAnswerOptions())
        pc1.setLocalDescription(offer)
        pc2.setRemoteDescription(offer)
        val answer = pc2.createAnswer(OfferAnswerOptions())
        pc2.setLocalDescription(answer)
        pc1.setRemoteDescription(answer)

        val message = withTimeout(5000) {
            dataChannel.onMessage.map { it.decodeToString() }.first()
        }
        assertEquals("Hello WebRTC KMP!", message)

        pc1.close()
        pc2.close()
    }
}
