package com.shepeliev.webrtckmp

import dev.onvoid.webrtc.media.video.VideoFrame
import dev.onvoid.webrtc.media.video.VideoTrackSink
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.Test
import kotlin.coroutines.resume
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