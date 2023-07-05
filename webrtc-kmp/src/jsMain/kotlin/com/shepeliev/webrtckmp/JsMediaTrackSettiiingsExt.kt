package com.shepeliev.webrtckmp

import org.w3c.dom.mediacapture.MediaTrackSettings as JsMediaTrackSettings

internal fun JsMediaTrackSettings.asCommon(): MediaTrackSettings = MediaTrackSettings(
    aspectRatio = aspectRatio,
    autoGainControl = autoGainControl,
    channelCount = channelCount,
    deviceId = deviceId,
    echoCancellation = echoCancellation,
    facingMode = facingMode?.asCommonFacingMode(),
    frameRate = frameRate,
    groupId = groupId,
    height = height,
    latency = latency,
    noiseSuppression = noiseSuppression,
    sampleRate = sampleRate,
    sampleSize = sampleSize,
    width = width
)
