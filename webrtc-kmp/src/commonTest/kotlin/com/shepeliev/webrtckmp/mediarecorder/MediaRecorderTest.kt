package com.shepeliev.webrtckmp.mediarecorder

import com.shepeliev.webrtckmp.MediaDevices
import com.shepeliev.webrtckmp.Platform
import com.shepeliev.webrtckmp.currentPlatform
import com.shepeliev.webrtckmp.initializeTestWebRtc
import com.shepeliev.webrtckmp.runTest
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MediaRecorderTest : CameraPermissionGrantedTest() {

    @BeforeTest
    fun beforeTest() {
        initializeTestWebRtc()
    }

    @Test
    fun should_record_with_no_time_slice_set() = runTest {
        // at the moment MediaRecorder is implemented for Android only
        if (currentPlatform != Platform.Android) return@runTest

        val stream = MediaDevices.getUserMedia(video = true)
        val mediaRecorder = MediaRecorder(stream)

        val outputFilePath = async { withTimeoutOrNull(10000) { mediaRecorder.onDataAvailable.first() } }
        val starts = async { withTimeoutOrNull(10000) { mediaRecorder.onStart.take(1).toList() } }
        val stops = async { withTimeoutOrNull(10000) { mediaRecorder.onStop.take(1).toList() } }
        val errorsJob = mediaRecorder.onError.onEach { throw it }.launchIn(this)
        yield()

        mediaRecorder.start()
        delay(5000)
        mediaRecorder.stop()

        assertFalse(outputFilePath.await().isNullOrBlank(), "No output file path")
        assertEquals(listOf(Unit), starts.await(), "No onStart event fired")
        assertEquals(listOf(Unit), stops.await(), "No onStop event fired")

        stream.release()
        errorsJob.cancel()
    }
}
