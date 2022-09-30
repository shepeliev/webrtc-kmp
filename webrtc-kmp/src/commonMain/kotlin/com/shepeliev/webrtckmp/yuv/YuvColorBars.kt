package com.shepeliev.webrtckmp.yuv

internal class YuvColorBars(width: Int, height: Int) {

    val width: Int
    val height: Int

    val yStrides: ByteArray by lazy { buildYStrides() }
    val uStrides: ByteArray by lazy { buildStrides(uBars) }
    val vStrides: ByteArray by lazy { buildStrides(vBars) }

    private val minYBarWidth = 2
    private val yBarWidth = maxOf(width / yBars.size, minYBarWidth)
    val uvStrideSize = width / 2 + width % 2
    private val numberOfUvStrides = height / 2 + height % 2
    private val uvBarWidth = yBarWidth / 2 + yBarWidth % 2

    init {
        require(width > -1) { "width can't be negative" }
        require(height > -1) { "height can't be negative" }
        this.width = width
        this.height = height
    }

    private fun buildYStrides(): ByteArray {
        val barsPerRow = yBars.size + if (width % yBars.size > 0) 1 else 0

        return ByteArray(width * height) {
            val col = it % width
            val barIdx = minOf((col / yBarWidth) % barsPerRow, yBars.size - 1)
            yBars[barIdx]
        }
    }

    private fun buildStrides(bars: ByteArray): ByteArray {
        val barsPerRow = uvStrideSize + if (uvStrideSize % bars.size > 0) 1 else 0

        return ByteArray(uvStrideSize * numberOfUvStrides) {
            val col = it % uvStrideSize
            val barIdx = minOf((col / uvBarWidth) % barsPerRow, bars.size - 1)
            bars[barIdx]
        }
    }
}

internal val yBars = byteArrayOf(
    0xeb.toByte(),
    0xa2.toByte(),
    0x84.toByte(),
    0x71.toByte(),
    0x54.toByte(),
    0x41.toByte(),
    0x23.toByte(),
    0x10.toByte()
)
internal val uBars = byteArrayOf(
    0x80.toByte(),
    0x2c,
    0x9d.toByte(),
    0x48,
    0xb8.toByte(),
    0x64,
    0xd4.toByte(),
    0x80.toByte()
)
internal val vBars = byteArrayOf(
    0x80.toByte(),
    0x8e.toByte(),
    0x2c,
    0x39,
    0xc7.toByte(),
    0xd4.toByte(),
    0x72,
    0x80.toByte()
)
