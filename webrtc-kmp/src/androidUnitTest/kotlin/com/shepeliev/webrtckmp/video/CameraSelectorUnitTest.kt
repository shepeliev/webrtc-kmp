package com.shepeliev.webrtckmp.video

import com.shepeliev.webrtckmp.FacingMode
import com.shepeliev.webrtckmp.MediaTrackConstraints
import com.shepeliev.webrtckmp.asValueConstrain
import org.webrtc.CameraEnumerationAndroid
import org.webrtc.CameraEnumerator
import org.webrtc.CameraVideoCapturer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CameraSelectorUnitTest {

    @Test
    fun testSelectCameraId_whenNoCameras() {
        val constraints = MediaTrackConstraints()
        val selector = CameraSelector()

        assertNull(selector.selectCameraId(NoCamerasEnumerator, constraints))
    }

    @Test
    fun testSelectCameraId_byDeviceId() {
        val constraints = MediaTrackConstraints(deviceId = "camera1")
        val selector = CameraSelector()

        assertEquals("camera1", selector.selectCameraId(TwoCamerasEnumerator, constraints))
    }

    @Test
    fun testSelectCameraId_byFacingMode_user() {
        val constraints = MediaTrackConstraints(facingMode = FacingMode.User.asValueConstrain())
        val selector = CameraSelector()

        assertEquals("camera1", selector.selectCameraId(TwoCamerasEnumerator, constraints))
    }

    @Test
    fun testSelectCameraId_byFacingMode_environment() {
        val constraints =
            MediaTrackConstraints(facingMode = FacingMode.Environment.asValueConstrain())
        val selector = CameraSelector()

        assertEquals("camera2", selector.selectCameraId(TwoCamerasEnumerator, constraints))
    }

    @Test
    fun testSelectCameraId_withoutConstraints() {
        val constraints = MediaTrackConstraints()
        val selector = CameraSelector()

        assertEquals("camera1", selector.selectCameraId(TwoCamerasEnumerator, constraints))
    }
}

private val NoCamerasEnumerator = object : CameraEnumerator {
    override fun getDeviceNames(): Array<String> = emptyArray()

    override fun isFrontFacing(cameraId: String?): Boolean =
        throw IllegalStateException("Unknown camera: $cameraId")

    override fun isBackFacing(cameraId: String?): Boolean =
        throw IllegalStateException("Unknown camera: $cameraId")

    override fun getSupportedFormats(cameraId: String?): List<CameraEnumerationAndroid.CaptureFormat> {
        return emptyList()
    }

    override fun createCapturer(
        cameraId: String, eventsHandler: CameraVideoCapturer.CameraEventsHandler?
    ): CameraVideoCapturer {
        throw UnsupportedOperationException()
    }
}
