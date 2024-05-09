package com.shepeliev.webrtckmp.internal

@JsName("console")
internal external object Console {
    fun log(message: String)
    fun warn(message: String)
    fun error(message: String)
}

internal fun Console.logln(message: String) = log("$message\n")
internal fun Console.warnln(message: String) = warn("$message\n")
internal fun Console.errorln(message: String) = error("$message\n")
