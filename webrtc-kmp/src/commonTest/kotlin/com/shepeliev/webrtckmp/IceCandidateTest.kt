package com.shepeliev.webrtckmp

import kotlin.test.Test
import kotlin.test.assertEquals

open class IceCandidateTest {

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
