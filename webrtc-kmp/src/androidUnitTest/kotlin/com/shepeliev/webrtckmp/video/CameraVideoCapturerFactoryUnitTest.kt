package com.shepeliev.webrtckmp.video

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CameraVideoCapturerFactoryUnitTest {
    @Test
    fun testCreateCameraVideoCapturer() {
        val factory = CameraVideoCapturerFactory()

        val capturer = factory.createCameraVideoCapturer("camera1", TwoCamerasEnumerator, null)

        assertNotNull(capturer)
    }

    @Test
    fun testCreateCameraVideoCapturer_deviceIdIsNull() {
        val factory = CameraVideoCapturerFactory()

        val capturer = factory.createCameraVideoCapturer(null, TwoCamerasEnumerator, null)

        assertNull(capturer)
    }
}
