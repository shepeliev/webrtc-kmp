package com.shepeliev.webrtckmp

import org.w3c.dom.mediacapture.MediaDevices
import org.w3c.dom.mediacapture.MediaStream
import org.w3c.dom.mediacapture.MediaStreamConstraints
import kotlin.js.Promise

@Suppress("UnsafeCastFromDynamic")
public fun MediaDevices.getDisplayMedia(): Promise<MediaStream> = this.asDynamic().getDisplayMedia()

@Suppress("UnsafeCastFromDynamic")
public fun MediaDevices.getDisplayMedia(constraints: MediaStreamConstraints): Promise<MediaStream> =
    this.asDynamic().getDisplayMedia(constraints)

@Suppress("UnsafeCastFromDynamic")
public fun MediaDevices.supportsDisplayMedia(): Boolean = this.asDynamic().getDisplayMedia != null
