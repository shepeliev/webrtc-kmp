package com.shepeliev.webrtckmp.internal

import com.shepeliev.webrtckmp.MediaTrackConstraints
import com.shepeliev.webrtckmp.ValueOrConstrain
import com.shepeliev.webrtckmp.externals.toFacingMode
import com.shepeliev.webrtckmp.map
import com.shepeliev.webrtckmp.value
import org.w3c.dom.mediacapture.ConstrainBooleanParameters
import org.w3c.dom.mediacapture.ConstrainDOMStringParameters
import org.w3c.dom.mediacapture.ConstrainDoubleRange
import org.w3c.dom.mediacapture.ConstrainULongRange
import org.w3c.dom.mediacapture.MediaTrackConstraintSet

internal fun MediaTrackConstraintSet.toMediaTrackConstraints() = MediaTrackConstraints(
    aspectRatio = aspectRatio?.toValueOrConstrain(),
    autoGainControl = autoGainControl?.toValueOrConstrain(),
    channelCount = channelCount?.toValueOrConstrain(),
    deviceId = deviceId?.toValueOrConstrain<String>()?.value,
    echoCancellation = echoCancellation?.toValueOrConstrain(),
    facingMode = facingMode?.toValueOrConstrain<String>()?.map { it.toFacingMode() },
    frameRate = frameRate?.toValueOrConstrain(),
    groupId = groupId?.toValueOrConstrain<String>()?.value,
    height = height?.toValueOrConstrain(),
    latency = latency?.toValueOrConstrain(),
    noiseSuppression = noiseSuppression?.toValueOrConstrain(),
    sampleRate = sampleRate?.toValueOrConstrain(),
    sampleSize = sampleSize?.toValueOrConstrain(),
    width = width?.toValueOrConstrain()
)

@Suppress("UNCHECKED_CAST")
internal inline fun <reified T> JsAny.toValueOrConstrain(): ValueOrConstrain<T>? {
    return when (T::class) {
        Int::class -> toIntValueOrConstrain() as ValueOrConstrain<T>?
        Double::class -> toDoubleValueOrConstrain() as ValueOrConstrain<T>?
        Boolean::class -> toBooleanValueOrConstrain() as ValueOrConstrain<T>?
        String::class -> toStringValueOrConstrain() as ValueOrConstrain<T>?
        else -> null
    }
}

private fun JsAny.toIntValueOrConstrain(): ValueOrConstrain<Int>? {
    return when {
        isJsNumber(this) && isInteger(this) -> ValueOrConstrain.Value(getInt(this))

        isObject(this) -> {
            val constrain = getConstrainULongRange(this)
            ValueOrConstrain.Constrain(constrain.exact, constrain.ideal)
        }

        else -> null
    }
}

private fun JsAny.toDoubleValueOrConstrain(): ValueOrConstrain<Double>? {
    return when {
        isJsNumber(this) -> ValueOrConstrain.Value(getDouble(this))

        isObject(this) -> {
            val constrain = getConstrainDoubleRange(this)
            ValueOrConstrain.Constrain(constrain.exact, constrain.ideal)
        }

        else -> null
    }
}

private fun JsAny.toBooleanValueOrConstrain(): ValueOrConstrain<Boolean>? {
    return when {
        isJsBoolean(this) -> ValueOrConstrain.Value(getBoolean(this))

        isObject(this) -> {
            val constrain = getConstrainBooleanParameters(this)
            ValueOrConstrain.Constrain(constrain.exact, constrain.ideal)
        }

        else -> null
    }
}

private fun JsAny.toStringValueOrConstrain(): ValueOrConstrain<String>? {
    return when {
        isJsString(this) -> ValueOrConstrain.Value(getString(this))
        isObject(this) -> {
            val constrain = getConstrainDOMStringParameters(this)
            ValueOrConstrain.Constrain(constrain.exact.toString(), constrain.ideal.toString())
        }

        else -> null
    }
}

@Suppress("UNUSED_PARAMETER")
private fun isJsNumber(value: JsAny): Boolean = js("typeof value === 'number'")

@Suppress("UNUSED_PARAMETER")
private fun isInteger(value: JsAny): Boolean = js("Number.isInteger(value)")

@Suppress("UNUSED_PARAMETER")
private fun isJsString(value: JsAny): Boolean = js("typeof value === 'string'")

@Suppress("UNUSED_PARAMETER")
private fun isJsBoolean(value: JsAny): Boolean = js("typeof value === 'boolean'")

@Suppress("UNUSED_PARAMETER")
private fun isObject(value: JsAny): Boolean = js("typeof value === 'object'")

@Suppress("UNUSED_PARAMETER")
private fun getInt(value: JsAny): Int = js("value")

@Suppress("UNUSED_PARAMETER")
private fun getDouble(value: JsAny): Double = js("value")

@Suppress("UNUSED_PARAMETER")
private fun getString(value: JsAny): String = js("value")

@Suppress("UNUSED_PARAMETER")
private fun getBoolean(value: JsAny): Boolean = js("value")

@Suppress("UNUSED_PARAMETER")
private fun getConstrainDoubleRange(value: JsAny): ConstrainDoubleRange = js("value")

@Suppress("UNUSED_PARAMETER")
private fun getConstrainBooleanParameters(value: JsAny): ConstrainBooleanParameters = js("value")

@Suppress("UNUSED_PARAMETER")
private fun getConstrainDOMStringParameters(value: JsAny): ConstrainDOMStringParameters = js("value")

@Suppress("UNUSED_PARAMETER")
private fun getConstrainULongRange(value: JsAny): ConstrainULongRange = js("value")
