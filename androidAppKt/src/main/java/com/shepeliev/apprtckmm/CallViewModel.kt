package com.shepeliev.apprtckmm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shepeliev.apprtckmm.shared.RoomConnectionParameters
import com.shepeliev.apprtckmm.shared.rtcclient.AppRtcClient
import com.shepeliev.apprtckmm.shared.rtcclient.WebSocketRtcClient
import com.shepeliev.webrtckmm.IceCandidate
import com.shepeliev.webrtckmm.SessionDescription
import kotlinx.coroutines.launch
import timber.log.Timber

class CallViewModel : ViewModel(), AppRtcClient.SignalingEvents {
    private val appRtcClient = WebSocketRtcClient(this)

    fun connectToRoom(roomUrl: String, roomId: String) = viewModelScope.launch {
        val params = RoomConnectionParameters(roomUrl, roomId, false)
        val signalingParams = appRtcClient.connectToRoom(params)
        Timber.d("$signalingParams")
    }

    override fun onRemoteDescription(sdp: SessionDescription) {
//        TODO("Not yet implemented")
    }

    override fun onRemoteIceCandidate(candidate: IceCandidate) {
//        TODO("Not yet implemented")
    }

    override fun onRemoteIceCandidatesRemoved(candidates: List<IceCandidate>) {
//        TODO("Not yet implemented")
    }

    override fun onChannelClose() {
//        TODO("Not yet implemented")
    }

    override fun onChannelError(description: String?) {
//        TODO("Not yet implemented")
    }
}