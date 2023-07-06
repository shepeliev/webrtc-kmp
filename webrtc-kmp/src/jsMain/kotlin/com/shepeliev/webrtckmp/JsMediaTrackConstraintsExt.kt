package com.shepeliev.webrtckmp

import org.w3c.dom.mediacapture.ConstrainBooleanParameters
import org.w3c.dom.mediacapture.ConstrainDOMStringParameters
import org.w3c.dom.mediacapture.ConstrainDoubleRange
import org.w3c.dom.mediacapture.ConstrainULongRange
import org.w3c.dom.mediacapture.MediaTrackConstraints as JsMediaTrackConstraints

internal fun ConstrainBooleanParameters?.asCommon(): ValueOrConstrain.Constrain<Boolean>? {
    this ?: return null
    return ValueOrConstrain.Constrain(exact, ideal)
}

internal fun ConstrainDOMStringParameters?.asCommon(): ValueOrConstrain.Constrain<String>? {
    this ?: return null
    return ValueOrConstrain.Constrain(exact as? String, ideal as? String)
}

internal fun ConstrainDoubleRange?.asCommon(): ValueOrConstrain.Constrain<Double>? {
    this ?: return null
    return ValueOrConstrain.Constrain(exact, ideal)
}

internal fun ConstrainULongRange?.asCommon(): ValueOrConstrain.Constrain<Int>? {
    this ?: return null
    return ValueOrConstrain.Constrain(exact, ideal)
}

internal fun ConstrainDOMStringParameters?.asCommonFacingModeConstrain(): ValueOrConstrain.Constrain<FacingMode>? {
    this ?: return null
    return ValueOrConstrain.Constrain(
        (exact as? String)?.asCommonFacingMode(),
        (ideal as? String)?.asCommonFacingMode()
    )
}

internal fun String.asCommonFacingMode() = when (this) {
    "user" -> FacingMode.User
    else -> FacingMode.Environment
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal fun JsMediaTrackConstraints.asCommon() = MediaTrackConstraints(
    aspectRatio = (aspectRatio as? Double)?.asValueConstrain()
        ?: (aspectRatio as? ConstrainDoubleRange)?.asCommon(),

    autoGainControl = (autoGainControl as? Boolean)?.asValueConstrain()
        ?: (autoGainControl as? ConstrainBooleanParameters)?.asCommon(),

    channelCount = (channelCount as? Number)?.toInt()?.asValueConstrain()
        ?: (channelCount as? ConstrainULongRange)?.asCommon(),

    deviceId = (deviceId as? String) ?: (deviceId.exact ?: deviceId.ideal) as? String,

    echoCancellation = (echoCancellation as? Boolean)?.asValueConstrain()
        ?: (echoCancellation as? ConstrainBooleanParameters)?.asCommon(),

    facingMode = (facingMode as? String)?.asCommonFacingMode()?.asValueConstrain()
        ?: (facingMode as? ConstrainDOMStringParameters).asCommonFacingModeConstrain(),

    frameRate = (frameRate as? Double)?.asValueConstrain()
        ?: (frameRate as? ConstrainDoubleRange)?.asCommon(),

    groupId = (groupId as? String) ?: (groupId.exact ?: groupId.ideal) as? String,

    height = (height as? Number)?.toInt()?.asValueConstrain()
        ?: (height as? ConstrainULongRange)?.asCommon(),

    latency = (latency as? Double)?.asValueConstrain()
        ?: (latency as? ConstrainDoubleRange)?.asCommon(),

    noiseSuppression = (noiseSuppression as? Boolean)?.asValueConstrain()
        ?: (noiseSuppression as? ConstrainBooleanParameters)?.asCommon(),

    sampleRate = (sampleRate as? Number)?.toInt()?.asValueConstrain()
        ?: (sampleRate as? ConstrainULongRange)?.asCommon(),

    sampleSize = (sampleSize as? Number)?.toInt()?.asValueConstrain()
        ?: (sampleSize as? ConstrainULongRange)?.asCommon(),

    width = (width as? Number)?.toInt()?.asValueConstrain()
        ?: (width as? ConstrainULongRange)?.asCommon(),
)
