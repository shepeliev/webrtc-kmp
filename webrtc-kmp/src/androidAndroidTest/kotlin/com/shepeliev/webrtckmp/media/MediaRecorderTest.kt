package com.shepeliev.webrtckmp.media

import com.shepeliev.webrtckmp.MediaDevices
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class MediaRecorderTest {

    @Test
    fun testVideoRecording() = runBlocking {
        val stream = MediaDevices.getUserMedia { video { deviceId("color-bars") } }
        val recorder = MediaRecorder(stream)

        val recordedFilePath = async { withTimeout(30.seconds) { recorder.onDataAvailable.first() } }
        recorder.start()
        delay(5.seconds)
        recorder.stop()

        val file = File(recordedFilePath.await())
        assertTrue(file.exists())
        assertTrue(file.length() > 0)
    }
}
