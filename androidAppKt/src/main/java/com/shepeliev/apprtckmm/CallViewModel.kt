package com.shepeliev.apprtckmm

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.shepeliev.apprtckmm.shared.RoomConnectionParameters
import com.shepeliev.apprtckmm.shared.SignalingParameters
import com.shepeliev.apprtckmm.shared.rtcclient.AppRtcClient
import com.shepeliev.apprtckmm.shared.rtcclient.PeerConnectionClient
import com.shepeliev.apprtckmm.shared.rtcclient.WebSocketRtcClient
import com.shepeliev.webrtckmm.IceCandidate
import com.shepeliev.webrtckmm.NativeVideoSinkAdapter
import com.shepeliev.webrtckmm.RtcStatsReport
import com.shepeliev.webrtckmm.SessionDescription
import kotlinx.coroutines.launch
import org.webrtc.VideoSink
import timber.log.Timber

class CallViewModel(app: Application) : AndroidViewModel(app), AppRtcClient.SignalingEvents,
    PeerConnectionClient.Companion.PeerConnectionEvents {
    private val appRtcClient = WebSocketRtcClient(this)
    private val pcClient = PeerConnectionClient(this, viewModelScope)
    private lateinit var signalingParams: SignalingParameters

    lateinit var remoteSink: VideoSink
    lateinit var localSink: VideoSink
    lateinit var navController: NavController

    private var logToast: Toast? = null

    private var isDisconnecting = false

    fun connectToRoom(roomUrl: String, roomId: String) = viewModelScope.launch {
        val params = RoomConnectionParameters(roomUrl, roomId, false)

        try {
            signalingParams = appRtcClient.connectToRoom(params)
        } catch (e: Throwable) {
            logAndToast("Connecting room failed: ${e.message}")
            navController.popBackStack()
        }
        pcClient.createPeerConnection(
            NativeVideoSinkAdapter(localSink),
            NativeVideoSinkAdapter(remoteSink),
            signalingParams
        )
        if (signalingParams.initiator) {
            logAndToast("Creating OFFER...")
            pcClient.createOffer()
        } else {
            if (signalingParams.offerSdp != null) {
                pcClient.setRemoteDescription(signalingParams.offerSdp!!)
                logAndToast("Creating ANSWER...")
                pcClient.createAnswer()
            }
            signalingParams.iceCandidates.forEach { pcClient.addRemoteIceCandidate(it) }
        }
    }

    // Log |msg| and Toast about it.
    private fun logAndToast(msg: String) {
        Timber.d(msg)
        logToast?.cancel()
        logToast = Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT)
        logToast?.show()
    }

    private fun disconnectWithErrorMessage(errorMessage: String) {
        logAndToast("Connection error: $errorMessage")
        disconnect()
    }

    fun disconnect() {
        if (isDisconnecting) return
        isDisconnecting = true
        viewModelScope.launch {
            logAndToast("Remote end hung up; dropping PeerConnection")
            appRtcClient.disconnectFromRoom()
            pcClient.close()
            navController.popBackStack()
        }
    }

    fun switchCamera() {
        viewModelScope.launch { pcClient.switchCamera() }
    }

    override fun onRemoteDescription(sdp: SessionDescription) {
        viewModelScope.launch {
            logAndToast("Received remote ${sdp.type}")
            pcClient.setRemoteDescription(sdp)
            if (!signalingParams.initiator) {
                pcClient.createAnswer()
            }
        }
    }

    override fun onRemoteIceCandidate(candidate: IceCandidate) {
        pcClient.addRemoteIceCandidate(candidate)
    }

    override fun onRemoteIceCandidatesRemoved(candidates: List<IceCandidate>) {
        pcClient.removeRemoteIceCandidates(candidates)
    }

    override fun onChannelClose() {
        disconnect()
    }

    override fun onChannelError(description: String?) {
        disconnectWithErrorMessage("$description")
    }

    override fun onLocalDescription(sdp: SessionDescription) {
        viewModelScope.launch {
            logAndToast("Sending ${sdp.type}")
            if (signalingParams.initiator) {
                appRtcClient.sendOfferSdp(sdp)
            } else {
                appRtcClient.sendAnswerSdp(sdp)
            }
        }
    }

    override fun onIceCandidate(candidate: IceCandidate) {
        viewModelScope.launch { appRtcClient.sendLocalIceCandidate(candidate) }
    }

    override fun onIceCandidatesRemoved(candidates: List<IceCandidate>) {
        viewModelScope.launch { appRtcClient.sendLocalIceCandidateRemovals(candidates) }
    }

    override fun onIceConnected() {
        logAndToast("ICE connected")
    }

    override fun onIceDisconnected() {
        logAndToast("ICE disconnected")
    }

    override fun onConnected() {
        logAndToast("DTLS connected")
    }

    override fun onDisconnected() {
        logAndToast("DTLS disconnected")
        disconnect()
    }

    override fun onPeerConnectionClosed() {
        logAndToast("PeerConnection closed")
    }

    override fun onPeerConnectionStatsReady(reports: Array<RtcStatsReport>) {}

    override fun onPeerConnectionError(description: String?) {
        disconnectWithErrorMessage("$description")
    }
}