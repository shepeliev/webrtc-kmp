@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp.capturer

import com.shepeliev.webrtckmp.MediaTrackConstraints
import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.com.shepeliev.webrtc.kmp.RTCVideoCapturerDelegateProtocol

internal expect class CameraVideoCapturerController(
    constraints: MediaTrackConstraints,
    videoCapturerDelegate: RTCVideoCapturerDelegateProtocol,
) : VideoCapturerController {

    override fun startCapture()
    override fun stopCapture()
    fun switchCamera()
    fun switchCamera(deviceId: String)
}
