package com.shepeliev.webrtckmm

import org.webrtc.MediaConstraints as NativeMediaConstraints

actual class MediaConstraints private constructor(val native: NativeMediaConstraints) {
    actual constructor(mandatory: Map<String, String>, optional: Map<String, String>) : this(
        NativeMediaConstraints().also {
            it.mandatory.addAll(
                mandatory.toList().map { (k, v) -> NativeMediaConstraints.KeyValuePair(k, v) }
            )

            it.optional.addAll(
                optional.toList().map { (k, v) -> NativeMediaConstraints.KeyValuePair(k, v) }
            )
        }
    )
}
