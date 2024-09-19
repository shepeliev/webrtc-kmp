@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp.capturer

import WebRTC.RTCVideoCapturerDelegateProtocol
import com.shepeliev.webrtckmp.MediaTrackConstraints
import kotlinx.cinterop.ExperimentalForeignApi

internal expect class CameraVideoCapturerController(
    constraints: MediaTrackConstraints,
    videoCapturerDelegate: RTCVideoCapturerDelegateProtocol,
) : VideoCapturerController {

    override fun startCapture()
    override fun stopCapture()
    fun switchCamera()
    fun switchCamera(deviceId: String)
}
