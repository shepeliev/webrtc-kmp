package com.shepeliev.webrtckmm.utils

import com.shepeliev.webrtckmm.MediaConstraints
import org.webrtc.MediaConstraints as NativeMediaConstraints

private fun Map<String, String>.toKVPairs(): List<NativeMediaConstraints.KeyValuePair> {
    return toList().map { (k, v) -> NativeMediaConstraints.KeyValuePair(k, v) }
}

fun MediaConstraints.toNative(): org.webrtc.MediaConstraints {
    return NativeMediaConstraints().apply {
        mandatory.addAll(this@toNative.mandatory.toKVPairs())
        optional.addAll(this@toNative.mandatory.toKVPairs())
    }
}
