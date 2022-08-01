package com.shepeliev.webrtckmp.sample

import android.app.Application
import com.shepeliev.webrtckmp.initializeWebRtc

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeWebRtc(this)
    }
}
