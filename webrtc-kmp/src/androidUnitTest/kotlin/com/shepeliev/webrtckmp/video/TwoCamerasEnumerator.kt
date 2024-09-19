package com.shepeliev.webrtckmp.video

import org.webrtc.CameraEnumerationAndroid
import org.webrtc.CameraEnumerator
import org.webrtc.CameraVideoCapturer

val TwoCamerasEnumerator = object : CameraEnumerator {
    override fun getDeviceNames(): Array<String> = arrayOf("camera1", "camera2")

    override fun isFrontFacing(cameraId: String?): Boolean = cameraId == "camera1"

    override fun isBackFacing(cameraId: String?): Boolean = cameraId == "camera2"

    override fun getSupportedFormats(cameraId: String?): List<CameraEnumerationAndroid.CaptureFormat> {
        return emptyList()
    }

    override fun createCapturer(
        cameraId: String,
        eventsHandler: CameraVideoCapturer.CameraEventsHandler?
    ): CameraVideoCapturer {
        check(cameraId in getDeviceNames()) { "Unknown camera: $cameraId" }
        return FakeCameraVideoCapturer(isFrontFacing = isFrontFacing(cameraId))
    }
}
