@file:OptIn(ExperimentalForeignApi::class)

package com.shepeliev.webrtckmp

import WebRTC.RTCDtmfSenderProtocol
import kotlinx.cinterop.ExperimentalForeignApi

public actual class DtmfSender(
    public val native: RTCDtmfSenderProtocol,
) {
    public actual val canInsertDtmf: Boolean get() = native.canInsertDtmf()
    public actual val duration: Int get() = (native.duration() * 1000).toInt()
    public actual val interToneGap: Int get() = (native.interToneGap() * 1000).toInt()

    public actual fun insertDtmf(
        tones: String,
        durationMs: Int,
        interToneGapMs: Int,
    ): Boolean = native.insertDtmf(tones, durationMs / 1000.0, interToneGapMs / 1000.0)

    public actual fun tones(): String = native.remainingTones()
}
