package com.shepeliev.webrtckmp

data class OfferAnswerOptions(
    val iceRestart: Boolean? = null,
    val offerToReceiveAudio: Boolean? = null,
    val offerToReceiveVideo: Boolean? = null,
    val voiceActivityDetection: Boolean? = null,
)
