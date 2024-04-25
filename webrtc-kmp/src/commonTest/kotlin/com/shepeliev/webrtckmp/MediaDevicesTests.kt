package com.shepeliev.webrtckmp

import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

@Ignore // for local tests only
class MediaDevicesTests {

    @BeforeTest
    fun setup() {
        setupMocks()
    }
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
