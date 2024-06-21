package com.shepeliev.webrtckmp

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

open class IceCandidateTest {

    @BeforeTest
    fun setup() {
        setupMocks()
    }

    @Test
    fun should_be_constructed_successfully() {
        val sdpMid = "sdpMid"
        val sdpMLineIndex = 42
        val candidate = "candidate"

        val iceCandidate = IceCandidate(sdpMid, sdpMLineIndex, candidate)

        assertEquals(sdpMid, iceCandidate.sdpMid)
        assertEquals(sdpMLineIndex, iceCandidate.sdpMLineIndex)
        assertEquals(candidate, iceCandidate.candidate)
    }
}
