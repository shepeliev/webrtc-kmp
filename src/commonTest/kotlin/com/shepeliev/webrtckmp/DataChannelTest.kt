package com.shepeliev.webrtckmp

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DataChannelTest {

    @BeforeTest
    fun setUp() {
        initializeWebRtc()
    }

    @AfterTest
    fun tearDown() {
        disposeWebRtc()
    }

    @Test
    fun data_channel_should_work() = runTest {
        val pc1 = PeerConnection()
        val pc2 = PeerConnection()

        val channel = Channel<String>()

        val pc1DataChannel = pc1.createDataChannel("dataChannel")!!.apply {
            registerObserver(object : DataChannelObserver {
                override fun onBufferedAmountChange(previousAmount: Long) {
                }

                override fun onStateChange() {
                }

                override fun onMessage(buffer: DataChannelBuffer) {
                    val text = buffer.data.decodeToString()
                    WebRtcKmp.mainScope.launch { channel.send(text) }
                }
            })
        }

        val pc1Candidates = mutableListOf<IceCandidate>()
        val pc2Candidates = mutableListOf<IceCandidate>()

        pc1.events.onIceCandidate
            .onEach { candidate ->
                if (pc2.signalingState == SignalingState.Stable) {
                    pc1Candidates.forEach { pc2.addIceCandidate(it) }
                    pc1Candidates.clear()
                    pc2.addIceCandidate(candidate)
                } else {
                    pc1Candidates += candidate
                }
            }
            .launchIn(WebRtcKmp.mainScope)

        pc2.events.onIceCandidate
            .onEach { candidate ->
                if (pc1.signalingState == SignalingState.Stable) {
                    pc2Candidates.forEach { pc1.addIceCandidate(it) }
                    pc2Candidates.clear()
                    pc1.addIceCandidate(candidate)
                } else {
                    pc2Candidates += candidate
                }
            }
            .launchIn(WebRtcKmp.mainScope)
        pc2.events.onDataChannel
            .onEach { pc2DataChannel ->
                val buffer = DataChannelBuffer("Hello WebRTC KMP!".encodeToByteArray(), false)
                pc2DataChannel.send(buffer)
            }
            .launchIn(WebRtcKmp.mainScope)

        val offer = pc1.createOffer(mediaConstraints())
        pc1.setLocalDescription(offer)
        pc2.setRemoteDescription(offer)
        val answer = pc2.createAnswer(mediaConstraints())
        pc2.setLocalDescription(answer)
        pc1.setRemoteDescription(answer)

        val text = withTimeout(5000) { channel.receive() }
        assertEquals("Hello WebRTC KMP!", text)

        pc1DataChannel.unregisterObserver()
    }
}
