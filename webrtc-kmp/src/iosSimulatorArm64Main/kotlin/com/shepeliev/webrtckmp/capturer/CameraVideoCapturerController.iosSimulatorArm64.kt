package com.shepeliev.webrtckmp.capturer

import com.shepeliev.webrtckmp.MediaTrackConstraints
import com.shepeliev.webrtckmp.WebRtc
import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.com.shepeliev.webrtc.kmp.RTCFileVideoCapturer
import swiftPMImport.com.shepeliev.webrtc.kmp.RTCLogEx
import swiftPMImport.com.shepeliev.webrtc.kmp.RTCLoggingSeverity
import swiftPMImport.com.shepeliev.webrtc.kmp.RTCVideoCapturerDelegateProtocol

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
