package com.shepeliev.webrtckmp

import android.content.Context
import androidx.startup.Initializer

@Suppress("unused")
internal class WebRtcInitializer : Initializer<WebRtc> {
    override fun create(context: Context): WebRtc {
        WebRtc.initialize(context)
        return WebRtc
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
