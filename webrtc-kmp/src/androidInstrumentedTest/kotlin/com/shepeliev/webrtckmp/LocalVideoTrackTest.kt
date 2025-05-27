package com.shepeliev.webrtckmp

import android.Manifest
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Rule
import org.webrtc.ApplicationContextProvider
import org.webrtc.VideoFrame
import org.webrtc.VideoSink
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

class LocalVideoTrackTest {
    @get:Rule
    val cameraPermission: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @BeforeTest
    fun setup() {
        WebRtc.initializeApplicationContext(ApplicationContextProvider.getApplicationContext())
    }

    @Test
    fun testVideoTrackCanBeRendered() = runTest(timeout = 5.seconds) {
        val stream = MediaDevices.getUserMedia(video = true)

        val videoTrack = stream.videoTracks.firstOrNull()
        assertNotNull(videoTrack, "Video track should not be null")

        val frameDeferred = CompletableDeferred<VideoFrame>()
        val sink = VideoSink { frameDeferred.complete(it) }

        videoTrack.addSink(sink)

        val frame = withContext(Dispatchers.Default) {
            withTimeoutOrNull(1000) { frameDeferred.await() }
        }
        assertNotNull(frame, "Video frame should not be null")

        videoTrack.removeSink(sink)
        stream.release()
    }
}
