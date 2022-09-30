package com.shepeliev.webrtckmp.yuv

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class YuvColorBarsTest {

    @Test
    fun test1x1() {
        val colorBars = YuvColorBars(width = 1, height = 1)

        assertEquals(1, colorBars.yStrides.size)
        assertEquals(1, colorBars.uStrides.size)
        assertEquals(1, colorBars.vStrides.size)

        assertContentEquals(byteArrayOf(yBars[0]), colorBars.yStrides)
        assertContentEquals(byteArrayOf(uBars[0]), colorBars.uStrides)
        assertContentEquals(byteArrayOf(vBars[0]), colorBars.vStrides)
    }

    @Test
    fun test2x1() {
        val colorBars = YuvColorBars(width = 2, height = 1)

        assertEquals(2, colorBars.yStrides.size)
        assertEquals(1, colorBars.uStrides.size)
        assertEquals(1, colorBars.vStrides.size)

        assertContentEquals(byteArrayOf(yBars[0], yBars[0]), colorBars.yStrides)
        assertContentEquals(byteArrayOf(uBars[0]), colorBars.uStrides)
        assertContentEquals(byteArrayOf(vBars[0]), colorBars.vStrides)
    }

    @Test
    fun test3x1() {
        val colorBars = YuvColorBars(width = 3, height = 1)

        assertEquals(3, colorBars.yStrides.size)
        assertEquals(2, colorBars.uStrides.size)
        assertEquals(2, colorBars.vStrides.size)

        assertContentEquals(byteArrayOf(yBars[0], yBars[0], yBars[1]), colorBars.yStrides)
        assertContentEquals(byteArrayOf(uBars[0], uBars[1]), colorBars.uStrides)
        assertContentEquals(byteArrayOf(vBars[0], vBars[1]), colorBars.vStrides)
    }

    @Test
    fun test8x1() {
        val colorBars = YuvColorBars(width = 8, height = 1)

        assertEquals(8, colorBars.yStrides.size)
        assertEquals(4, colorBars.uStrides.size)
        assertEquals(4, colorBars.vStrides.size)

        assertContentEquals(
            byteArrayOf(yBars[0], yBars[0], yBars[1], yBars[1], yBars[2], yBars[2], yBars[3], yBars[3]),
            colorBars.yStrides
        )
        assertContentEquals(byteArrayOf(uBars[0], uBars[1], uBars[2], uBars[3]), colorBars.uStrides)
        assertContentEquals(byteArrayOf(vBars[0], vBars[1], vBars[2], vBars[3]), colorBars.vStrides)
    }

    @Test
    fun test16x1() {
        val colorBars = YuvColorBars(width = 16, height = 1)

        assertEquals(16, colorBars.yStrides.size)
        assertEquals(8, colorBars.uStrides.size)
        assertEquals(8, colorBars.vStrides.size)

        assertContentEquals(
            byteArrayOf(
                yBars[0], yBars[0], yBars[1], yBars[1], yBars[2], yBars[2], yBars[3], yBars[3],
                yBars[4], yBars[4], yBars[5], yBars[5], yBars[6], yBars[6], yBars[7], yBars[7],
            ),
            colorBars.yStrides
        )
        assertContentEquals(
            byteArrayOf(uBars[0], uBars[1], uBars[2], uBars[3], uBars[4], uBars[5], uBars[6], uBars[7]),
            colorBars.uStrides
        )
        assertContentEquals(
            byteArrayOf(vBars[0], vBars[1], vBars[2], vBars[3], vBars[4], vBars[5], vBars[6], vBars[7]),
            colorBars.vStrides
        )
    }

    @Test
    fun test16x2() {
        val colorBars = YuvColorBars(width = 16, height = 2)

        assertEquals(32, colorBars.yStrides.size)
        assertEquals(8, colorBars.uStrides.size)
        assertEquals(8, colorBars.vStrides.size)

        assertContentEquals(
            byteArrayOf(
                yBars[0], yBars[0], yBars[1], yBars[1], yBars[2], yBars[2], yBars[3], yBars[3],
                yBars[4], yBars[4], yBars[5], yBars[5], yBars[6], yBars[6], yBars[7], yBars[7],
                yBars[0], yBars[0], yBars[1], yBars[1], yBars[2], yBars[2], yBars[3], yBars[3],
                yBars[4], yBars[4], yBars[5], yBars[5], yBars[6], yBars[6], yBars[7], yBars[7],
            ),
            colorBars.yStrides
        )
        assertContentEquals(
            byteArrayOf(uBars[0], uBars[1], uBars[2], uBars[3], uBars[4], uBars[5], uBars[6], uBars[7]),
            colorBars.uStrides
        )
        assertContentEquals(
            byteArrayOf(vBars[0], vBars[1], vBars[2], vBars[3], vBars[4], vBars[5], vBars[6], vBars[7]),
            colorBars.vStrides
        )
    }

    @Test
    fun test16x3() {
        val colorBars = YuvColorBars(width = 16, height = 3)

        assertEquals(48, colorBars.yStrides.size)
        assertEquals(16, colorBars.uStrides.size)
        assertEquals(16, colorBars.vStrides.size)

        assertContentEquals(
            byteArrayOf(
                yBars[0], yBars[0], yBars[1], yBars[1], yBars[2], yBars[2], yBars[3], yBars[3],
                yBars[4], yBars[4], yBars[5], yBars[5], yBars[6], yBars[6], yBars[7], yBars[7],
                yBars[0], yBars[0], yBars[1], yBars[1], yBars[2], yBars[2], yBars[3], yBars[3],
                yBars[4], yBars[4], yBars[5], yBars[5], yBars[6], yBars[6], yBars[7], yBars[7],
                yBars[0], yBars[0], yBars[1], yBars[1], yBars[2], yBars[2], yBars[3], yBars[3],
                yBars[4], yBars[4], yBars[5], yBars[5], yBars[6], yBars[6], yBars[7], yBars[7],
            ),
            colorBars.yStrides
        )
        assertContentEquals(
            byteArrayOf(
                uBars[0], uBars[1], uBars[2], uBars[3], uBars[4], uBars[5], uBars[6], uBars[7],
                uBars[0], uBars[1], uBars[2], uBars[3], uBars[4], uBars[5], uBars[6], uBars[7],
            ),
            colorBars.uStrides
        )
        assertContentEquals(
            byteArrayOf(
                vBars[0], vBars[1], vBars[2], vBars[3], vBars[4], vBars[5], vBars[6], vBars[7],
                vBars[0], vBars[1], vBars[2], vBars[3], vBars[4], vBars[5], vBars[6], vBars[7],
            ),
            colorBars.vStrides
        )
    }

    @Test
    fun test16x4() {
        val colorBars = YuvColorBars(width = 16, height = 4)

        assertEquals(64, colorBars.yStrides.size)
        assertEquals(16, colorBars.uStrides.size)
        assertEquals(16, colorBars.vStrides.size)

        assertContentEquals(
            byteArrayOf(
                yBars[0], yBars[0], yBars[1], yBars[1], yBars[2], yBars[2], yBars[3], yBars[3],
                yBars[4], yBars[4], yBars[5], yBars[5], yBars[6], yBars[6], yBars[7], yBars[7],
                yBars[0], yBars[0], yBars[1], yBars[1], yBars[2], yBars[2], yBars[3], yBars[3],
                yBars[4], yBars[4], yBars[5], yBars[5], yBars[6], yBars[6], yBars[7], yBars[7],
                yBars[0], yBars[0], yBars[1], yBars[1], yBars[2], yBars[2], yBars[3], yBars[3],
                yBars[4], yBars[4], yBars[5], yBars[5], yBars[6], yBars[6], yBars[7], yBars[7],
                yBars[0], yBars[0], yBars[1], yBars[1], yBars[2], yBars[2], yBars[3], yBars[3],
                yBars[4], yBars[4], yBars[5], yBars[5], yBars[6], yBars[6], yBars[7], yBars[7],
            ),
            colorBars.yStrides
        )
        assertContentEquals(
            byteArrayOf(
                uBars[0], uBars[1], uBars[2], uBars[3], uBars[4], uBars[5], uBars[6], uBars[7],
                uBars[0], uBars[1], uBars[2], uBars[3], uBars[4], uBars[5], uBars[6], uBars[7],
            ),
            colorBars.uStrides
        )
        assertContentEquals(
            byteArrayOf(
                vBars[0], vBars[1], vBars[2], vBars[3], vBars[4], vBars[5], vBars[6], vBars[7],
                vBars[0], vBars[1], vBars[2], vBars[3], vBars[4], vBars[5], vBars[6], vBars[7],
            ),
            colorBars.vStrides
        )
    }

    @Test
    fun test17x4() {
        val colorBars = YuvColorBars(width = 17, height = 4)

        assertEquals(68, colorBars.yStrides.size)
        assertEquals(18, colorBars.uStrides.size)
        assertEquals(18, colorBars.vStrides.size)

        assertContentEquals(
            byteArrayOf(
                yBars[0], yBars[0], yBars[1], yBars[1], yBars[2], yBars[2], yBars[3], yBars[3],
                yBars[4], yBars[4], yBars[5], yBars[5], yBars[6], yBars[6], yBars[7], yBars[7],
                yBars[7],

                yBars[0], yBars[0], yBars[1], yBars[1], yBars[2], yBars[2], yBars[3], yBars[3],
                yBars[4], yBars[4], yBars[5], yBars[5], yBars[6], yBars[6], yBars[7], yBars[7],
                yBars[7],


                yBars[0], yBars[0], yBars[1], yBars[1], yBars[2], yBars[2], yBars[3], yBars[3],
                yBars[4], yBars[4], yBars[5], yBars[5], yBars[6], yBars[6], yBars[7], yBars[7],
                yBars[7],

                yBars[0], yBars[0], yBars[1], yBars[1], yBars[2], yBars[2], yBars[3], yBars[3],
                yBars[4], yBars[4], yBars[5], yBars[5], yBars[6], yBars[6], yBars[7], yBars[7],
                yBars[7],
            ),
            colorBars.yStrides
        )
        assertContentEquals(
            byteArrayOf(
                uBars[0], uBars[1], uBars[2], uBars[3], uBars[4], uBars[5], uBars[6], uBars[7], uBars[7],
                uBars[0], uBars[1], uBars[2], uBars[3], uBars[4], uBars[5], uBars[6], uBars[7], uBars[7],
            ),
            colorBars.uStrides
        )
        assertContentEquals(
            byteArrayOf(
                vBars[0], vBars[1], vBars[2], vBars[3], vBars[4], vBars[5], vBars[6], vBars[7], vBars[7],
                vBars[0], vBars[1], vBars[2], vBars[3], vBars[4], vBars[5], vBars[6], vBars[7], vBars[7],
            ),
            colorBars.vStrides
        )
    }

    @Test
    fun test24x1() {
        val colorBars = YuvColorBars(width = 24, height = 1)

        assertEquals(24, colorBars.yStrides.size)
        assertEquals(12, colorBars.uStrides.size)
        assertEquals(12, colorBars.vStrides.size)

        assertContentEquals(
            byteArrayOf(
                yBars[0], yBars[0], yBars[0], yBars[1], yBars[1], yBars[1], yBars[2], yBars[2], yBars[2],
                yBars[3], yBars[3], yBars[3], yBars[4], yBars[4], yBars[4], yBars[5], yBars[5], yBars[5],
                yBars[6], yBars[6], yBars[6], yBars[7], yBars[7], yBars[7],
            ),
            colorBars.yStrides
        )
        assertContentEquals(
            byteArrayOf(
                uBars[0], uBars[0], uBars[1], uBars[1], uBars[2], uBars[2],
                uBars[3], uBars[3], uBars[4], uBars[4], uBars[5], uBars[5],
            ),
            colorBars.uStrides
        )
        assertContentEquals(
            byteArrayOf(
                vBars[0], vBars[0], vBars[1], vBars[1], vBars[2], vBars[2],
                vBars[3], vBars[3], vBars[4], vBars[4], vBars[5], vBars[5],
            ),
            colorBars.vStrides
        )
    }
}
