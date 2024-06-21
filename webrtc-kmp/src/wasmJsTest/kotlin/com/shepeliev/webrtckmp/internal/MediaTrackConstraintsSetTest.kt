package com.shepeliev.webrtckmp.internal

import com.shepeliev.webrtckmp.ValueOrConstrain
import kotlin.test.Test
import kotlin.test.assertEquals

class MediaTrackConstraintsSetTest {
    @Test
    fun testToValueOrConstrainAsValue() {
        assertEquals(ValueOrConstrain.Value(42), jsValue(42).toValueOrConstrain<Int>())
        assertEquals(ValueOrConstrain.Value(42.0), jsValue(42).toValueOrConstrain<Double>())
        assertEquals(ValueOrConstrain.Value(true), jsValue(true).toValueOrConstrain<Boolean>())
        assertEquals(ValueOrConstrain.Value("exact"), jsValue("exact").toValueOrConstrain<String>())
    }

    @Test
    fun testToValueOrConstrainAsConstrain() {
        assertEquals(
            ValueOrConstrain.Constrain(42, 43),
            jsConstrainParameters(42, 43).toValueOrConstrain<Int>()
        )
        assertEquals(
            ValueOrConstrain.Constrain(42.0, 43.0),
            jsConstrainParameters(42, 43).toValueOrConstrain<Double>()
        )
        assertEquals(
            ValueOrConstrain.Constrain(exact = true, ideal = true),
            jsConstrainParameters(exact = true, ideal = true).toValueOrConstrain<Boolean>()
        )
        assertEquals(
            ValueOrConstrain.Constrain("exact", "ideal"),
            jsConstrainParameters("exact", "ideal").toValueOrConstrain<String>()
        )
    }

}

@Suppress("UNUSED_PARAMETER")
private fun jsValue(value: Int): JsAny = js("value")

@Suppress("UNUSED_PARAMETER")
private fun jsValue(value: Boolean): JsAny = js("value")

@Suppress("UNUSED_PARAMETER")
private fun jsValue(value: String): JsAny = js("value")

@Suppress("UNUSED_PARAMETER")
private fun jsConstrainParameters(exact: Int, ideal: Int): JsAny = js("({exact: exact, ideal: ideal})")

@Suppress("UNUSED_PARAMETER")
private fun jsConstrainParameters(exact: Boolean, ideal: Boolean): JsAny = js("({exact: exact, ideal: ideal})")

@Suppress("UNUSED_PARAMETER")
private fun jsConstrainParameters(exact: String, ideal: String): JsAny = js("({exact: exact, ideal: ideal})")
