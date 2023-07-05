package com.shepeliev.webrtckmp

import kotlin.test.Test
import kotlin.test.assertEquals

class MediaStreamConstraintsBuilderTest {
    @Test
    fun should_work() {
        val builder = MediaStreamConstraintsBuilder()

        builder.audio {
            deviceId("audio_device_id")
            groupId("audio_group_id")
            autoGainControl()
            channelCount { ideal(42) }
            echoCancellation { exact(true) }
            latency(42.0)
            noiseSuppression(true)
            sampleRate(42)
        }

        builder.video {
            deviceId("video_device_id")
            groupId("video_group_id")
            aspectRatio(42.0)
            facingMode(FacingMode.User)
            frameRate(42.0)
            height(720)
            width(1280)
        }

        assertEquals(
            MediaStreamConstraints(
                audio = MediaTrackConstraints(
                    deviceId = "audio_device_id",
                    groupId = "audio_group_id",
                    autoGainControl = true.asValueConstrain(),
                    channelCount = ValueOrConstrain.Constrain(ideal = 42),
                    echoCancellation = ValueOrConstrain.Constrain(exact = true),
                    latency = 42.0.asValueConstrain(),
                    noiseSuppression = true.asValueConstrain(),
                    sampleRate = 42.asValueConstrain(),
                ),

                video = MediaTrackConstraints(
                    deviceId = "video_device_id",
                    groupId = "video_group_id",
                    aspectRatio = 42.0.asValueConstrain(),
                    facingMode = FacingMode.User.asValueConstrain(),
                    frameRate = 42.0.asValueConstrain(),
                    height = 720.asValueConstrain(),
                    width = 1280.asValueConstrain(),
                )
            ),

            builder.constraints
        )
    }
}
