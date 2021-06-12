package com.shepeliev.webrtckmp

expect fun runTest(block: suspend () -> Unit)

expect fun initialize()

expect fun disposeWebRtc()
