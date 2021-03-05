package com.shepeliev.apprtckmm.shared

import com.shepeliev.apprtckmm.shared.rtcclient.AppRtcClient
import com.shepeliev.apprtckmm.shared.rtcclient.WebSocketRtcClient
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
        val signalingParameters = client.connectToRoom(params)

        assertTrue(signalingParameters.clientId.isNotEmpty())
        assertTrue(signalingParameters.iceServers.isNotEmpty())
    }
}

private class SignalingEvents : AppRtcClient.SignalingEvents {
    override fun onRemoteDescription(sdp: SessionDescription) {
    }

    override fun onRemoteIceCandidate(candidate: IceCandidate) {
    }

    override fun onRemoteIceCandidatesRemoved(candidates: List<IceCandidate>) {
    }

    override fun onChannelClose() {
    }

    override fun onChannelError(description: String?) {
    }
}
