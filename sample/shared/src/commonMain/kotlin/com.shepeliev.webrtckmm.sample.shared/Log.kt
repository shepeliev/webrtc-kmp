package com.shepeliev.webrtckmm.sample.shared

expect object Log {
    fun i(tag: String, msg: String, throwable: Throwable? = null)
    fun d(tag: String, msg: String, throwable: Throwable? = null)
    fun w(tag: String, msg: String, throwable: Throwable? = null)
    fun e(tag: String, msg: String, throwable: Throwable? = null)
}
