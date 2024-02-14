package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.CreateSessionDescriptionObserver
import dev.onvoid.webrtc.RTCAnswerOptions
import dev.onvoid.webrtc.RTCOfferOptions
import dev.onvoid.webrtc.RTCPeerConnection
import dev.onvoid.webrtc.RTCSessionDescription
import dev.onvoid.webrtc.SetSessionDescriptionObserver
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun RTCPeerConnection.createOffer(options: RTCOfferOptions = RTCOfferOptions()): RTCSessionDescription = suspendCancellableCoroutine {
    createOffer(
        options,
        object : CreateSessionDescriptionObserver {
            override fun onSuccess(description: RTCSessionDescription) {
                it.resume(description)
            }

            override fun onFailure(error: String) {
                it.resumeWithException(RuntimeException(error))
            }
        },
    )
}

internal suspend fun RTCPeerConnection.setLocalDescription(description: RTCSessionDescription) = suspendCancellableCoroutine {
    setLocalDescription(
        description,
        object : SetSessionDescriptionObserver {
            override fun onSuccess() {
                it.resume(Unit)
            }

            override fun onFailure(error: String) {
                it.resumeWithException(RuntimeException(error))
            }
        },
    )
}

internal suspend fun RTCPeerConnection.createAnswer(options: RTCAnswerOptions = RTCAnswerOptions()): RTCSessionDescription = suspendCancellableCoroutine {
    createAnswer(
        options,
        object : CreateSessionDescriptionObserver {
            override fun onSuccess(description: RTCSessionDescription) {
                it.resume(description)
            }

            override fun onFailure(error: String) {
                it.resumeWithException(RuntimeException(error))
            }
        },
    )
}

internal suspend fun RTCPeerConnection.setRemoteDescription(description: RTCSessionDescription) = suspendCancellableCoroutine {
    setRemoteDescription(
        description,
        object : SetSessionDescriptionObserver {
            override fun onSuccess() {
                it.resume(Unit)
            }

            override fun onFailure(error: String) {
                it.resumeWithException(RuntimeException(error))
            }
        },
    )
}
