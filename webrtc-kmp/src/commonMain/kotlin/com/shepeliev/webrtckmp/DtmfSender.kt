package com.shepeliev.webrtckmp

public expect class DtmfSender {
    public val canInsertDtmf: Boolean
    public val duration: Int
    public val interToneGap: Int

    public fun insertDtmf(
        tones: String,
        durationMs: Int = 300,
        interToneGapMs: Int = 50,
    ): Boolean

    public fun tones(): String
}
