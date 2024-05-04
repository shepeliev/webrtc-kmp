package com.shepeliev.webrtckmp.internal

import com.shepeliev.webrtckmp.OfferAnswerOptions

internal fun OfferAnswerOptions.toWasmJs(): JsAny {
    return createOfferAnswerOptions(
        iceRestart = iceRestart,
        offerToReceiveAudio = offerToReceiveAudio,
        offerToReceiveVideo = offerToReceiveVideo,
        voiceActivityDetection = voiceActivityDetection
    )
}

@Suppress("UNUSED_PARAMETER")
private fun createOfferAnswerOptions(
    iceRestart: Boolean?,
    offerToReceiveAudio: Boolean?,
    offerToReceiveVideo: Boolean?,
    voiceActivityDetection: Boolean?
): JsAny = js(
    """
    ({
        iceRestart: iceRestart,
        offerToReceiveAudio: offerToReceiveAudio,
        offerToReceiveVideo: offerToReceiveVideo,
        voiceActivityDetection: voiceActivityDetection
    })"""
)
