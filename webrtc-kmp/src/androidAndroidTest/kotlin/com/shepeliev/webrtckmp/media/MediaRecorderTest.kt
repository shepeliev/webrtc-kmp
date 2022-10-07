package com.shepeliev.webrtckmp.media

import android.Manifest
import androidx.test.rule.GrantPermissionRule
import com.shepeliev.webrtckmp.MediaDevices
import com.shepeliev.webrtckmp.WebRtc
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.webrtc.Logging
import java.io.File
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class MediaRecorderTest {

    @get:Rule
    val grantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
    )

    @Before
    fun before() {
        WebRtc.configureBuilder {
            loggingSeverity = Logging.Severity.LS_VERBOSE
        }
    }

    @Test
    fun testVideoRecording() = runBlocking {
        val stream = MediaDevices.getUserMedia(video = true)
        val recorder = MediaRecorder(stream)

        val recordedFilePath = async { withTimeout(30.seconds) { recorder.onDataAvailable.first() } }
        recorder.start()
        delay(5.seconds)
        recorder.stop()

        val file = File(recordedFilePath.await())
        assertTrue(file.exists())
        assertTrue(file.length() > 0)
    }

    @Test
    fun testAudioRecording() = runBlocking {
        val stream = MediaDevices.getUserMedia(audio = true)
        val recorder = MediaRecorder(stream)

        val recordedFilePath = async { withTimeout(30.seconds) { recorder.onDataAvailable.first() } }
        recorder.start()
        delay(5.seconds)
        recorder.stop()

        val file = File(recordedFilePath.await())
        assertTrue(file.exists())
        assertTrue(file.length() > 0)
    }

    @Test
    fun testAudioAndVideoRecording() = runBlocking {
        val stream = MediaDevices.getUserMedia(audio = true, video = true)
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
