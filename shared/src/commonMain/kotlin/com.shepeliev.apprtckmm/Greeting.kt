package com.shepeliev.apprtckmm

import com.shepeliev.webrtckmm.Platform

class Greeting {
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }
}
