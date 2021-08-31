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
            volume(42.0)
        }

        builder.video {
            deviceId("video_device_id")
            groupId("video_group_id")
            aspectRatio(42.0)
            facingMode(FacingMode.User)
            frameRate(42.0)
            height(720)
            width(1280)
            resizeMode(ResizeMode.CropAndScale)
        }

        assertEquals(
            MediaStreamConstraints(
                audio = AudioTrackConstraints(
                    deviceId = "audio_device_id",
                    groupId = "audio_group_id",
                    autoGainControl = Constrain(exact = true),
                    channelCount = Constrain(ideal = 42),
                    echoCancellation = Constrain(exact = true),
                    latency = Constrain(exact = 42.0),
                    noiseSuppression = Constrain(exact = true),
                    sampleRate = Constrain(exact = 42),
                    volume = Constrain(exact = 42.0)
                ),

                video = VideoTrackConstraints(
                    deviceId = "video_device_id",
                    groupId = "video_group_id",
                    aspectRatio = Constrain(exact = 42.0),
                    facingMode = Constrain(exact = FacingMode.User),
                    frameRate = Constrain(exact = 42.0),
                    height = Constrain(exact = 720),
                    width = Constrain(exact = 1280),
                    resizeMode = Constrain(exact = ResizeMode.CropAndScale)
                )
            ),

            builder.constraints
        )
    }
}

// MediaStreamConstraints(audio=AudioTrackConstraints(deviceId=audio_device_id, groupId=audio_group_id, autoGainControl=Constrain(exact=true, ideal=null), channelCount=Constrain(exact=null, ideal=42), echoCancellation=Constrain(exact=true, ideal=null), latency=Constrain(exact=42.0, ideal=null), noiseSuppression=Constrain(exact=true, ideal=null), sampleRate=Constrain(exact=42, ideal=null), volume=Constrain(exact=42.0, ideal=null)), video=VideoTrackConstraints(deviceId=video_device_id, groupId=video_group_id, aspectRatio=Constrain(exact=42.0, ideal=null), facingMode=Constrain(exact=User, ideal=null), frameRate=Constrain(exact=42.0, ideal=null), height=Constrain(exact=720, ideal=null), width=Constrain(exact=1280, ideal=null), resizeMode=Constrain(exact=CropAndScale, ideal=null)))
// MediaStreamConstraints(audio=AudioTrackConstraints(deviceId=audio_device_id, groupId=audio_group_id, autoGainControl=Constrain(exact=true, ideal=null), channelCount=Constrain(exact=null, ideal=42), echoCancellation=Constrain(exact=true, ideal=null), latency=Constrain(exact=42.0, ideal=null), noiseSuppression=null, sampleRate=Constrain(exact=42, ideal=null), volume=Constrain(exact=42.0, ideal=null)), video=VideoTrackConstraints(deviceId=video_device_id, groupId=video_group_id, aspectRatio=Constrain(exact=42.0, ideal=null), facingMode=Constrain(exact=User, ideal=null), frameRate=Constrain(exact=42.0, ideal=null), height=Constrain(exact=720, ideal=null), width=Constrain(exact=1280, ideal=null), resizeMode=Constrain(exact=CropAndScale, ideal=null)))
