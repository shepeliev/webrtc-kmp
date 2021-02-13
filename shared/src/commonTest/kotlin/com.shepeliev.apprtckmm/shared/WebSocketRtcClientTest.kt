package com.shepeliev.apprtckmm.shared

import com.shepeliev.apprtckmm.AppRtcClient
import com.shepeliev.apprtckmm.RoomConnectionParameters
import com.shepeliev.apprtckmm.WebSocketRtcClient
import com.shepeliev.apprtckmm.uuid
import com.shepeliev.webrtckmm.IceCandidate
import com.shepeliev.webrtckmm.SessionDescription
import kotlin.test.Test
import kotlin.test.assertTrue

class WebSocketRtcClientTest {

    @Test
    fun test_connect_to_room() = runTest {
        val client = WebSocketRtcClient(SignalingEvents())
        val roomId = uuid()

        val params = RoomConnectionParameters(
            roomUrl = "https://appr.tc",
            roomId = roomId,
            loopback = false,
        )
        val signalingParameters =  client.connectToRoom(params)

        assertTrue(signalingParameters.clientId.isNotEmpty())
        assertTrue(signalingParameters.iceServers.isNotEmpty())
    }
}

private class SignalingEvents : AppRtcClient.SignalingEvents {
    override suspend fun onRemoteDescription(sdp: SessionDescription) {
    }

    override suspend fun onRemoteIceCandidate(candidate: IceCandidate) {
    }

    override suspend fun onRemoteIceCandidatesRemoved(candidates: List<IceCandidate>) {
    }

    override suspend fun onChannelClose() {
    }

    override suspend fun onChannelError(description: String?) {
    }
}
