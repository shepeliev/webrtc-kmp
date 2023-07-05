package com.shepeliev.webrtckmp

import org.w3c.dom.mediacapture.ConstrainBooleanParameters
import org.w3c.dom.mediacapture.ConstrainDOMStringParameters
import org.w3c.dom.mediacapture.ConstrainDoubleRange
import org.w3c.dom.mediacapture.ConstrainULongRange
import kotlin.test.Test
import kotlin.test.assertEquals
import org.w3c.dom.mediacapture.MediaTrackConstraints as JsMediaTrackConstraints

class JsMediaTrackConstraintsExtTest {
    @Test
    fun test_jsMediaTrackConstraints_with_primitive_values_asCommon() {
        val jsMediaTrackConstraints = JsMediaTrackConstraints(
            aspectRatio = 1.0,
            autoGainControl = true,
            channelCount = 1,
            deviceId = "deviceId",
            echoCancellation = true,
            facingMode = "user",
            frameRate = 1.0,
            groupId = "groupId",
            height = 480,
            latency = 1.0,
            noiseSuppression = true,
            sampleRate = 42,
            sampleSize = 42,
            width = 640,
        )

        val commonMediaTrackConstraints = jsMediaTrackConstraints.asCommon()

        assertEquals(
            MediaTrackConstraints(
                aspectRatio = 1.0.asValueConstrain(),
                autoGainControl = true.asValueConstrain(),
                channelCount = 1.asValueConstrain(),
                deviceId = "deviceId",
                echoCancellation = true.asValueConstrain(),
                facingMode = FacingMode.User.asValueConstrain(),
                frameRate = 1.0.asValueConstrain(),
                groupId = "groupId",
                height = 480.asValueConstrain(),
                latency = 1.0.asValueConstrain(),
                noiseSuppression = true.asValueConstrain(),
                sampleRate = 42.asValueConstrain(),
                sampleSize = 42.asValueConstrain(),
                width = 640.asValueConstrain(),
            ),

            commonMediaTrackConstraints
        )
    }

    @Test
    fun test_jsMediaConstraints_with_constrain_values_asCommon() {
        val jsMediaTrackConstraints = JsMediaTrackConstraints(
            aspectRatio = ConstrainDoubleRange(1.0, 42.0),
            autoGainControl = ConstrainBooleanParameters(exact = true, ideal = true),
            channelCount = ConstrainULongRange(1, 42),
            deviceId = ConstrainDOMStringParameters("deviceId", "idealDeviceId"),
            echoCancellation = ConstrainBooleanParameters(exact = true, ideal = false),
            facingMode = ConstrainDOMStringParameters("user", "environment"),
            frameRate = ConstrainDoubleRange(1.0, 42.0),
            groupId = ConstrainDOMStringParameters("groupId", "idealGroupId"),
            height = ConstrainULongRange(480, 720),
            latency = ConstrainDoubleRange(1.0, 42.0),
            noiseSuppression = ConstrainBooleanParameters(exact = true, ideal = true),
            sampleRate = ConstrainULongRange(1, 42),
            sampleSize = ConstrainULongRange(1, 42),
            width = ConstrainULongRange(640, 1280),
        )

        val commonMediaTrackConstraints = jsMediaTrackConstraints.asCommon()

        assertEquals(
            MediaTrackConstraints(
                aspectRatio = ValueOrConstrain.Constrain(1.0, 42.0),
                autoGainControl = ValueOrConstrain.Constrain(exact = true, ideal = true),
                channelCount = ValueOrConstrain.Constrain(1, 42),
                deviceId = "deviceId",
                echoCancellation = ValueOrConstrain.Constrain(exact = true, ideal = false),
                facingMode = ValueOrConstrain.Constrain(FacingMode.User, FacingMode.Environment),
                frameRate = ValueOrConstrain.Constrain(1.0, 42.0),
                groupId = "groupId",
                height = ValueOrConstrain.Constrain(480, 720),
                latency = ValueOrConstrain.Constrain(1.0, 42.0),
                noiseSuppression = ValueOrConstrain.Constrain(exact = true, ideal = true),
                sampleRate = ValueOrConstrain.Constrain(1, 42),
                sampleSize = ValueOrConstrain.Constrain(1, 42),
                width = ValueOrConstrain.Constrain(640, 1280),
            ),

            commonMediaTrackConstraints
        )
    }
}
