package com.shepeliev.webrtckmp.video

import WebRTC.RTCFileVideoCapturer
import WebRTC.RTCLogEx
import WebRTC.RTCLoggingSeverity
import WebRTC.RTCVideoCapturerDelegateProtocol
import com.shepeliev.webrtckmp.MediaTrackConstraints
import com.shepeliev.webrtckmp.WebRtc
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
internal actual class CameraVideoCapturerController actual constructor(
    constraints: MediaTrackConstraints,
    private val videoCapturerDelegate: RTCVideoCapturerDelegateProtocol
) : VideoCapturerController() {

    private var videoCapturer: RTCFileVideoCapturer? = null

    actual override fun startCapture() {
        val videoCapturer = RTCFileVideoCapturer(delegate = videoCapturerDelegate)
            .also { videoCapturer = it }

        videoCapturer.startCapturingFromFileNamed(WebRtc.simulatorCameraFallbackFileName) { error ->
            if (error == null) return@startCapturingFromFileNamed
            RTCLogEx(
                severity = RTCLoggingSeverity.RTCLoggingSeverityError,
                log_string = "Error starting video capture: ${error.localizedDescription}"
            )
        }

        settings = settings.copy(deviceId = WebRtc.simulatorCameraFallbackFileName)
    }

    actual override fun stopCapture() {
        videoCapturer?.stopCapture()
        videoCapturer = null
    }

    actual fun switchCamera() {
        RTCLogEx(
            severity = RTCLoggingSeverity.RTCLoggingSeverityWarning,
            log_string = "Camera switching is not supported in simulator"
        )
    }

    actual fun switchCamera(deviceId: String) {
        RTCLogEx(
            severity = RTCLoggingSeverity.RTCLoggingSeverityWarning,
            log_string = "Camera switching is not supported in simulator"
        )
    }
}
