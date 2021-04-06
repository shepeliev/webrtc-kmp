package com.shepeliev.webrtckmm.sample.shared

import android.util.Log

actual object Log {
    actual fun i(tag: String, msg: String, throwable: Throwable?) {
        Log.i(tag, msg, throwable)
    }

    actual fun d(tag: String, msg: String, throwable: Throwable?) {
        Log.d(tag, msg, throwable)
    }

    actual fun w(tag: String, msg: String, throwable: Throwable?) {
        Log.w(tag, msg, throwable)
    }

    actual fun e(tag: String, msg: String, throwable: Throwable?) {
        Log.e(tag, msg, throwable)
    }
}
