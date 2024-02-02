package com.shepeliev.webrtckmp

import org.junit.Test
import kotlin.test.assertTrue

class MediaDevicesTests {

    @Test
    fun enumerateDevices() = runTest {
        val devices = MediaDevices.enumerateDevices()
        assertTrue(devices.isNotEmpty())
    }

    @Test
    fun getUserMedia() = runTest {
        val mediaStream = MediaDevices.getUserMedia {
            audio()
            video()
        }

        mediaStream.release()
    }
}
