package com.shepeliev.webrtckmp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@Ignore // for local tests only
class MediaDevicesTests {

    private val scope = TestScope()

    @BeforeTest
    fun setup() {
        setupMocks()
        Dispatchers.setMain(StandardTestDispatcher(scope.testScheduler))
    }
    @Test
    fun enumerateDevices() = runTest(timeout = 5.seconds) {
        val devices = MediaDevices.enumerateDevices()
        assertTrue(devices.isNotEmpty())
    }

    @Test
    fun getUserMedia() = runTest(timeout = 5.seconds) {
        val mediaStream = MediaDevices.getUserMedia {
            audio()
            video()
        }

        mediaStream.release()
    }
}
